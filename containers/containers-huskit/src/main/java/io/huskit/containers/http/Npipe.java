package io.huskit.containers.http;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.function.ThrowingFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

final class Npipe implements DockerSocket {

    MemoizedSupplier<Npipe.State> stateSupplier;

    Npipe(String socketFile) {
        this.stateSupplier = MemoizedSupplier.of(() -> new Npipe.State(socketFile));
    }

    @Override
    public <T> Http.Response<T> send(Http.Request request, ThrowingFunction<HttpFlow, List<T>> action) {
        return sendAsyncInternal(request, action).join();
    }

    @Override
    public <T> CompletableFuture<Http.Response<T>> sendAsync(Http.Request request, ThrowingFunction<HttpFlow, List<T>> action) {
        return sendAsyncInternal(request, action);
    }

    private <T> CompletableFuture<Http.Response<T>> sendAsyncInternal(Http.Request request, ThrowingFunction<HttpFlow, List<T>> action) {
        var state = stateSupplier.get();
        return state
                .write(request)
                .thenCompose(r -> read(
                                new ReadState<>(
                                        action,
                                        state.decoder
                                )
                        )
                );
    }

    private <T> CompletableFuture<Http.Response<T>> read(ReadState<T> readState) {
        return readAndProcess(readState)
                .thenCompose(resultReadState -> {
                    if (resultReadState.shouldReadMore) {
                        return readAndProcess(resultReadState);
                    } else {
                        return CompletableFuture.completedFuture(resultReadState);
                    }
                })
                .thenApply(ReadState::toResponse);
    }

    private <T> CompletableFuture<ReadState<T>> readAndProcess(ReadState<T> readState) {
        return readOnce(readState)
                .thenCompose(r -> {
                    processBuffer(r);
                    if (r.shouldReadMore) {
                        return readAndProcess(r);
                    } else {
                        return CompletableFuture.completedFuture(r);
                    }
                })
                .exceptionally(ex -> {
                    throw new RuntimeException(ex);
                });
    }

    @SneakyThrows
    private <T> void processBuffer(ReadState<T> readState) {
        var readBuffer = readState.readBuffer;
        readBuffer.flip();
        var chars = readState.decoder.decode(readBuffer);
        var shouldReadMore = readState.currentResult == readState.readBufferSize;
        readState.shouldReadMore = shouldReadMore;
        if (readState.isHead) {
            while (chars.hasRemaining()) {
                var ch = chars.get();
                var prevCh = readState.prevChar;
                var isCrLf = ch == '\n' && prevCh == '\r';
                if (isCrLf) {
                    if (readState.prevLineWasEmpty) {
                        readState.isHead = false;
                        readState.isChunked = "chunked".equals(readState.headers.get("Transfer-Encoding"));
                        break;
                    }
                } else {
                    if (readState.prevLineWasEmpty && ch != '\n' && ch != '\r') {
                        readState.prevLineWasEmpty = false;
                    }
                }
                if (readState.lineCount == 0) {
                    if (ch == ' ' && readState.status == -1) {
                        readState.status = (chars.get() - '0') * 100 + (chars.get() - '0') * 10 + (chars.get() - '0');
                    }
                } else if (readState.isHeaderKeyPart) {
                    if (ch == ':') {
                        readState.isHeaderKeyPart = false;
                        readState.lineBuffer.flip();
                        readState.currentHeaderKey = readState.lineBuffer.toString();
                        readState.lineBuffer.clear();
                        readState.isHeaderValuePart = true;
                        readState.isHeaderValueSkipPart = true;
                    } else {
                        if (!isCrLf) {
                            readState.writeToLineBuffer(ch);
                        }
                    }
                } else if (readState.isHeaderValuePart) {
                    if (readState.isHeaderValueSkipPart) {
                        readState.isHeaderValueSkipPart = false;
                    } else {
                        if (isCrLf) {
                            readState.lineBuffer.flip();
                            readState.lineBuffer.limit(readState.lineBuffer.limit() - 1);
                            var headerVal = readState.lineBuffer.toString();
                            readState.headers.put(readState.currentHeaderKey, headerVal);
                        } else {
                            readState.writeToLineBuffer(ch);
                        }
                    }
                }
                if (isCrLf) {
                    readState.prevLineWasEmpty = true;
                    readState.lineCount++;
                    readState.isHeaderKeyPart = true;
                    readState.isHeaderValuePart = false;
                    readState.lineBuffer.clear();
                }
                readState.prevChar = ch;
            }
        }
        if (!readState.isHead) {
            if (readState.status == 204) {
                readState.shouldReadMore = false;
            } else {
                if (readState.isChunked) {
                    System.out.println("chunked");
                } else {
                    if (!shouldReadMore) {
                        if (chars.array()[chars.limit() - 1] == '\n') {
                            chars.limit(chars.limit() - 1);
                        }
                    }
                    readState.writeToBodyBuffer(chars);
                }
            }
        }
        readBuffer.clear();
    }

    private <T> CompletableFuture<ReadState<T>> readOnce(ReadState<T> readState) {
        var readOpCompletion = new CompletableFuture<ReadState<T>>();
        var state = stateSupplier.get();
        state.channel.read(readState.readBuffer, 0, readState, new CompletionHandler<>() {

            @Override
            public void completed(Integer result, ReadState<T> readState) {
                readState.currentResult = result;
                readOpCompletion.complete(readState);
            }

            @Override
            public void failed(Throwable exc, ReadState readState) {
                readOpCompletion.completeExceptionally(exc);
            }
        });
        return readOpCompletion;
    }


    @Getter
    private static class State {

        AsynchronousFileChannel channel;
        CharsetDecoder decoder;
        CharsetEncoder encoder;

        @SneakyThrows
        private State(String socketFile) {
            this.channel = AsynchronousFileChannel.open(
                    Paths.get(socketFile),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE
            );
            this.decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.encoder = StandardCharsets.UTF_8.newEncoder();
        }

        public CompletableFuture<Integer> write(Http.Request request) {
            var writeCompletion = new CompletableFuture<Integer>();
            channel.write(ByteBuffer.wrap(request.body()), 0, null, new CompletionHandler<>() {

                @Override
                public void completed(Integer result, Object attachment) {
                    writeCompletion.complete(result);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    writeCompletion.completeExceptionally(exc);
                }
            });
            return writeCompletion;
        }
    }

    @SneakyThrows
    public void close() {
        if (stateSupplier.isInitialized()) {
            stateSupplier.get().channel.close();
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class MyHttpResponse<T> implements Http.Response<T> {

        Http.Head head;
        Http.Body<T> body;
    }

    @Getter
    @RequiredArgsConstructor
    public static class HttpFlow {

        Reader reader;
        Http.Head head;
    }

    @RequiredArgsConstructor
    private static class ReadState<T> {

        ThrowingFunction<HttpFlow, List<T>> action;
        CharsetDecoder decoder;
        int readBufferSize = 2048;
        int lineBufferSize = 128;
        @NonFinal
        Integer currentResult;
        @NonFinal
        int lineCount;
        @NonFinal
        int status = -1;
        @NonFinal
        boolean isHead = true;
        @NonFinal
        char prevChar;
        @NonFinal
        boolean prevLineWasEmpty = false;
        @NonFinal
        boolean isHeaderKeyPart = true;
        @NonFinal
        boolean isHeaderValuePart = false;
        @NonFinal
        boolean isHeaderValueSkipPart = false;
        Map<String, String> headers = new HashMap<>();
        ByteBuffer readBuffer = ByteBuffer.allocate(readBufferSize);
        @NonFinal
        CharBuffer lineBuffer = CharBuffer.allocate(lineBufferSize);
        @NonFinal
        CharBuffer bodyBuffer;
        @NonFinal
        boolean shouldReadMore = false;
        @NonFinal
        boolean isChunked = false;
        @NonFinal
        String currentHeaderKey;

        public void writeToLineBuffer(char ch) {
            if (lineBuffer.remaining() == 0) {
                var newBuffer = CharBuffer.allocate(lineBuffer.capacity() * 2);
                lineBuffer.flip();
                newBuffer.put(lineBuffer);
                lineBuffer = newBuffer;
            }
            lineBuffer.put(ch);
        }

        public void writeToBodyBuffer(CharBuffer chars) {
            if (bodyBuffer == null) {
                bodyBuffer = CharBuffer.allocate(readBufferSize);
            }
            if (bodyBuffer.remaining() < chars.remaining()) {
                var newBuffer = CharBuffer.allocate(bodyBuffer.capacity() + chars.remaining());
                bodyBuffer.flip();
                newBuffer.put(bodyBuffer);
                bodyBuffer = newBuffer;
            }
            bodyBuffer.put(chars);
        }

        @SneakyThrows
        public MyHttpResponse<T> toResponse() {
            if (shouldReadMore || status == -1) {
                throw new IllegalStateException("Cannot create response from incomplete state");
            }
            bodyBuffer.flip();
            var body = bodyBuffer.toString();
            System.out.println("body -> " + body);
            return new MyHttpResponse<>(
                    new DfHead(status, headers),
                    new DfBody<>(
                            action.apply(
                                    new HttpFlow(
                                            new StringReader(body),
                                            new DfHead(
                                                    status,
                                                    headers
                                            )
                                    )
                            )
                    )
            );
        }
    }
}
