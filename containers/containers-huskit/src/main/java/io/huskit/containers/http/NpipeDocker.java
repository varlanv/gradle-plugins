package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.HtConstants;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

final class NpipeDocker implements DockerSocket {

    MemoizedSupplier<NpipeDocker.State> stateSupplier;

    NpipeDocker(String socketFile) {
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeDocker.State(socketFile));
    }

    public NpipeDocker() {
        this(HtConstants.NPIPE_SOCKET);
    }

    @Override
    public Http.RawResponse send(Request request) {
        return sendAsyncInternal(request).join();
    }

    @Override
    public CompletableFuture<Http.RawResponse> sendAsync(Request request) {
        return sendAsyncInternal(request);
    }

    private CompletableFuture<Http.RawResponse> sendAsyncInternal(Request request) {
        var state = stateSupplier.get();
        var rawResponse = new CompletableFuture<Http.RawResponse>();
        CompletableFuture.runAsync(() -> {
            try {
                state.write(request);
                readAndProcess(
                        new ReadState(
                                request,
                                state.decoder,
                                rawResponse
                        )
                );
            } finally {
                if (state.isDirtyConnection().get()) {
                    state.resetConnection();
                }
                state.lock.release();
            }
        });
        return CompletableFuture.completedFuture(rawResponse.join());
    }

    private void readAndProcess(ReadState rs) {
        read(rs);
        processBuffer(rs);
        checkResult(rs);
    }

    private void checkResult(ReadState rs) {
        if (rs.shouldReadMore) {
            readAndProcess(rs);
        } else {
            rs.request.repeatReadPredicate()
                    .ifPresentOrElse(repeatRead -> {
                                var backoff = repeatRead.backoff().toMillis();
                                var predicate = repeatRead.lookFor().predicate();
                                if (repeatRead.lookFor().isOnlyInStdErr()) {
                                    rs.buildStrderrStream().filter(predicate)
                                            .findFirst()
                                            .ifPresent(ignore -> rs.isKeepReading = false);
                                } else if (repeatRead.lookFor().isOnlyInStdOut()) {
                                    rs.buildStdoutStream()
                                            .filter(predicate)
                                            .findFirst()
                                            .ifPresent(ignore -> rs.isKeepReading = false);
                                } else {
                                    Stream.concat(rs.buildStdoutStream(), rs.buildStrderrStream())
                                            .filter(predicate)
                                            .findFirst()
                                            .ifPresent(ignore -> rs.isKeepReading = false);
                                }
                                if (rs.isKeepReading) {
                                    try {
                                        if (backoff > 0) {
                                            Thread.sleep(backoff);
                                        }
                                        var nrs = new ReadState(rs);
                                        nrs.isHead = false;
                                        readAndProcess(nrs);
                                    } catch (InterruptedException e) {
                                        throw Sneaky.rethrow(e);
                                    }
                                } else {
                                    rs.response.complete(
                                            new Http.RawResponse.StdRawResponse(
                                                    new DfHead(
                                                            200,
                                                            rs.headers
                                                    ),
                                                    rs.stdoutPipe,
                                                    rs.stderrPipe
                                            )
                                    );
                                }
                            },
                            rs::breakPipes
                    );
        }
    }


    @SneakyThrows
    private void processBuffer(ReadState readState) {
        readState.readBuffer.flip();
        readState.chars = readState.decoder.decode(readState.readBuffer);
        if (readState.isHead) {
            processHead(readState);
        }
        if (!readState.isHead) {
            processBody(readState);
        }
        readState.decoder.reset();
        readState.readBuffer.clear();
    }

    private void processHead(ReadState readState) {
        while (readState.chars.hasRemaining()) {
            var ch = readState.chars.get();
            var prevCh = readState.prevChar;
            var isCrLf = ch == '\n' && prevCh == '\r';
            if (isCrLf) {
                if (readState.prevLineWasEmpty) {
                    readState.isHead = false;
                    readState.isChunked = "chunked".equals(readState.headers.get("Transfer-Encoding"));
                    readState.isMultiplexedStream = "application/vnd.docker.multiplexed-stream".equals(readState.headers.get("Content-Type"));
                    readState.prevLineWasEmpty = false;
                    readState.prevChar = 0;
                    break;
                }
            } else {
                if (readState.prevLineWasEmpty && ch != '\n' && ch != '\r') {
                    readState.prevLineWasEmpty = false;
                }
            }
            if (readState.headLineCount == 0) {
                if (readState.statusReads >= 0 && readState.statusReads <= 3) {
                    if (readState.statusReads == 0) {
                        readState.status = (ch - '0') * 100;
                    } else if (readState.statusReads == 1) {
                        readState.status = readState.status + (ch - '0') * 10;
                    } else if (readState.statusReads == 2) {
                        readState.status = readState.status + (ch - '0');
                    }
                    readState.statusReads++;
                } else {
                    if (ch == ' ' && readState.status == -1) {
                        readState.statusReads++;
                    }
                }
            } else if (readState.isHeaderKeyPart) {
                if (ch == ':') {
                    readState.isHeaderKeyPart = false;
                    readState.lineBuffer.flip();
                    readState.currentHeaderKey = readState.lineBuffer.toString();
                    readState.clearLineBuffer();
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
                readState.headLineCount++;
                readState.isHeaderKeyPart = true;
                readState.isHeaderValuePart = false;
                readState.clearLineBuffer();
            }
            readState.prevChar = ch;
        }
    }

    private void processBody(ReadState readState) {
        readState.complete();
        if (readState.status == 204) {
            readState.shouldReadMore = false;
            readState.bodyNotPresent = true;
        } else {
            if (readState.isChunked) {
                if (readState.chars.remaining() == 0) {
                    readState.shouldReadMore = true;
                } else {
                    if (readState.bodyLineCount == 0) {
                        readState.isChunkSizeEndPart = true;
                    }
                    while (readState.chars.hasRemaining()) {
                        var ch = readState.chars.get();
                        if (readState.isChunkSizeEndPart) {
                            if (ch == '\r') {
                                readState.chunkSizeBuffer.flip();
                                readState.currentChunkSize = Integer.parseInt(readState.chunkSizeBuffer.toString(), 16);
                                readState.chunkSizeBuffer.clear();
                                readState.currentReadChunkSize = readState.currentChunkSize;
                                readState.isChunkSizeEndPart = false;
                                if (readState.currentChunkSize == 0) {
                                    readState.shouldReadMore = false;
                                    break;
                                }
                                if (readState.chars.hasRemaining()) {
                                    readState.chars.get();
                                    readState.bodyLineCount++;
                                }
                            } else {
                                readState.chunkSizeBuffer.put(ch);
                            }
                        } else if (readState.isChunkSizeStartPart) {
                            if (ch == '\n') {
                                readState.isChunkSizeEndPart = true;
                            }
                        } else if (readState.isMultiplexedStream) {
                            readState.bodyNotPresent = true;
                            readState.currentReadChunkSize--;
                            if (readState.isMultiplexLineStart) {
                                if (ch == '\u0001') {
                                    readState.isMultiplexStderr = false;
                                } else if (ch == '\u0002') {
                                    readState.isMultiplexStderr = true;
                                } else if (ch != '\u0000') {
                                    readState.isMultiplexLineStart = false;
                                    readState.multiplexLineSize = ch;
                                }
                            } else {
                                if (--readState.multiplexLineSize == 0) {
                                    if (readState.isMultiplexStderr) {
                                        readState.writeToStderrBuffer(ch);
                                    } else {
                                        readState.writeToStdoutBuffer(ch);
                                    }
                                    if (readState.currentReadChunkSize == 0) {
                                        readState.isChunkSizeStartPart = true;
                                        if (readState.request.repeatReadPredicate().isPresent()) {
                                            readState.shouldReadMore = false;
                                        }
                                    } else {
                                        readState.isMultiplexLineStart = true;
                                        readState.bodyLineCount++;
                                    }
                                } else {
                                    if (readState.isMultiplexStderr) {
                                        readState.writeToStderrBuffer(ch);
                                    } else {
                                        readState.writeToStdoutBuffer(ch);
                                    }
                                }
                            }
                        } else {
                            readState.writeToBody(ch);
                            if (--readState.currentReadChunkSize == 0) {
                                readState.bodyLineCount++;
                                if (readState.chars.hasRemaining()) {
                                    readState.chars.get();
                                    if (readState.chars.hasRemaining()) {
                                        readState.chars.get();
                                    }
                                }
                                readState.isChunkSizeEndPart = true;
                            }
                        }
                    }
                }
            } else {
                if (!readState.shouldReadMore && readState.chars.array()[readState.chars.limit() - 1] == '\n') {
                    readState.chars.limit(readState.chars.limit() - 1);
                }
                readState.writeToBody(readState.chars);
            }
        }
    }

    @SneakyThrows
    private void read(ReadState readState) {
        readState.resultSize = stateSupplier.get().channel.read(readState.readBuffer, 0).get();
    }


    @Getter
    private static final class State {

        @NonFinal
        AsynchronousFileChannel channel;
        String socketFile;
        CharsetDecoder decoder;
        CharsetEncoder encoder;
        Semaphore lock;
        AtomicBoolean isDirtyConnection;

        private State(String socketFile) {
            this.socketFile = socketFile;
            this.channel = openChannel();
            this.decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.encoder = StandardCharsets.UTF_8.newEncoder();
            this.lock = new Semaphore(1);
            this.isDirtyConnection = new AtomicBoolean(false);
        }

        @SneakyThrows
        private AsynchronousFileChannel openChannel() {
            return AsynchronousFileChannel.open(
                    Paths.get(socketFile),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE
            );
        }

        @SneakyThrows
        public void write(Request request) {
            var body = request.http().body();
            var bb = ByteBuffer.wrap(body);
            lock.acquire();
            System.out.println("Sending request body -> " + new String(body, StandardCharsets.UTF_8));
            channel.write(bb, 0).get();
            if (request.repeatReadPredicate().isPresent()) {
                isDirtyConnection.set(true);
            }
        }

        @SneakyThrows
        public void resetConnection() {
            channel.close();
            channel = openChannel();
            isDirtyConnection().set(false);
        }
    }

    @SneakyThrows
    public void release() {
        if (stateSupplier.isInitialized()) {
            stateSupplier.get().channel().close();
        }
    }

    private static final class ReadState {

        Request request;
        CharsetDecoder decoder;
        CompletableFuture<Http.RawResponse> response;
        SimplePipe stdoutPipe;
        SimplePipe stderrPipe;
        int readBufferSize = 4096;
        int lineBufferSize = 256;
        int chunkSizeBufferSize = 16;
        ByteBuffer readBuffer = ByteBuffer.allocate(readBufferSize);
        @NonFinal
        boolean isMultiplexStderr;
        @NonFinal
        int multiplexLineSize;
        @NonFinal
        boolean isMultiplexedStream;
        @NonFinal
        boolean isMultiplexLineStart = true;
        @NonFinal
        CharBuffer lineBuffer = CharBuffer.allocate(lineBufferSize);
        SimplePipe bodyPipe = new SimplePipe(readBufferSize);
        CharBuffer chunkSizeBuffer = CharBuffer.allocate(chunkSizeBufferSize);
        @NonFinal
        CharBuffer chars;
        Map<String, String> headers = new HashMap<>();
        @NonFinal
        String currentHeaderKey;
        @NonFinal
        int headLineCount;
        @NonFinal
        int status = -1;
        @NonFinal
        int statusReads = -1;
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
        @NonFinal
        boolean shouldReadMore = false;
        @NonFinal
        boolean isChunked = false;
        @NonFinal
        boolean bodyNotPresent = false;
        @NonFinal
        int currentChunkSize = -1;
        @NonFinal
        int currentReadChunkSize = 0;
        @NonFinal
        boolean isChunkSizeStartPart = false;
        @NonFinal
        boolean isChunkSizeEndPart = false;
        @NonFinal
        int bodyLineCount;
        @NonFinal
        int resultSize;
        @NonFinal
        boolean isKeepReading = true;
        @NonFinal
        boolean isCompleted;

        public ReadState(Request request,
                         CharsetDecoder decoder,
                         CompletableFuture<Http.RawResponse> response,
                         SimplePipe stdoutPipe,
                         SimplePipe stderrPipe) {
            this.request = request;
            this.decoder = decoder;
            this.response = response;
            this.stdoutPipe = stdoutPipe;
            this.stderrPipe = stderrPipe;
        }

        public ReadState(Request request,
                         CharsetDecoder decoder,
                         CompletableFuture<Http.RawResponse> response) {
            this(
                    request,
                    decoder,
                    response,
                    new SimplePipe(4096),
                    new SimplePipe(4096)
            );
        }

        public ReadState(ReadState rs) {
            this(
                    rs.request,
                    rs.decoder,
                    rs.response,
                    rs.stdoutPipe,
                    rs.stderrPipe
            );
        }

        public void complete() {
            if (!isCompleted) {
                isCompleted = true;
                if (status == -1) {
                    throw new IllegalStateException("Cannot create response from incomplete state");
                }
                var head = new DfHead(status, headers);
                request.expectedStatus()
                        .map(ExpectedStatus::status)
                        .ifPresent(expectedStatus -> {
                            if (!Objects.equals(status, expectedStatus)) {
                                throw new RuntimeException(
                                        String.format(
                                                "Expected status %d but received %d",
                                                expectedStatus, status
                                        )
                                );
                            }
                        });
                if (request.repeatReadPredicate().isEmpty()) {
                    if (isMultiplexedStream) {
                        stdoutPipe.init();
                        stderrPipe.init();
                        response.complete(
                                new Http.RawResponse.StdRawResponse(
                                        head,
                                        stdoutPipe,
                                        stderrPipe
                                )
                        );
                    } else {
                        bodyPipe.init();
                        response.complete(
                                new Http.RawResponse.BodyRawResponse(
                                        head,
                                        bodyPipe::source
                                )
                        );
                    }
                }
            }
        }

        public void writeToStdoutBuffer(char ch) {
            stdoutPipe.write(ch);
        }

        public void writeToStderrBuffer(char ch) {
            stderrPipe.write(ch);
        }

        public void clearLineBuffer() {
            lineBuffer.clear();
        }

        public void writeToBody(char ch) {
            bodyPipe.write(ch);
        }

        public void writeToBody(CharBuffer chars) {
            bodyPipe.writeLine(chars);
        }

        public void writeToLineBuffer(char ch) {
            if (lineBuffer.remaining() == 0) {
                var newBuffer = CharBuffer.allocate(lineBuffer.capacity() * 2);
                lineBuffer.flip();
                newBuffer.put(lineBuffer);
                lineBuffer = newBuffer;
            }
            lineBuffer.put(ch);
        }

        private Stream<String> buildStdoutStream() {
            return new PipeStream(stdoutPipe).streamSupplier().get();
        }

        private Stream<String> buildStrderrStream() {
            return new PipeStream(stderrPipe).streamSupplier().get();
        }

        public void breakPipes() {
            stdoutPipe.breakPipe();
            stderrPipe.breakPipe();
            bodyPipe.breakPipe();
        }
    }
}
