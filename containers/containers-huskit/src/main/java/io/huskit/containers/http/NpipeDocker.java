package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.HtConstants;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
                                rawResponse
                        )
                );
            } finally {
                if (state.isDirtyConnection().get()) {
                    state.resetConnection();
                }
                state.releaseLock();
            }
        }).handle((ignore, throwable) -> {
            if (throwable != null) {
                rawResponse.completeExceptionally(throwable);
            }
            return null;
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
                                if (rs.shouldReadMore) {
                                    try {
                                        Thread.sleep(repeatRead.backoff().toMillis());
                                        readAndProcess(new ReadState(rs));
                                    } catch (Exception e) {
                                        throw Sneaky.rethrow(e);
                                    }
                                } else {
                                    rs.response.complete(
                                            new Http.RawResponse.StdRawResponse(
                                                    new DfHead(200, new HashMap<>()),
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
        var copy = ByteBuffer.wrap(readState.readBuffer.array().clone());
        copy.limit(readState.readBuffer.limit());
        copy.position(readState.readBuffer.position());
        var clone = Arrays.copyOfRange(copy.array(), copy.position(), copy.limit());
        var fullString = new String(clone, StandardCharsets.UTF_8);
        var bais = new ByteArrayInputStream(copy.array(), copy.position(), copy.limit());
        var sb = new StringBuilder();
        int i;
        while ((i = bais.read()) != -1) {
            var ch = (char) i;
            sb.append(ch);
            var kek = 0;
            kek++;
        }
        if (readState.isHead) {
            processHead(readState);
        }
        if (!readState.isHead) {
            processBody(readState);
        }
        readState.readBuffer.clear();
    }

    private void processHead(ReadState readState) {
        while (readState.hasNext()) {
            var i = readState.readNext();
            var ch = (char) i;
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
                if (!readState.hasNext()) {
                    readState.shouldReadMore = true;
                } else {
                    if (readState.bodyLineCount == 0) {
                        readState.isChunkSizeEndPart = true;
                    }
                    while (readState.hasNext()) {
                        var i = readState.readNext();
                        var ch = (char) i;
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
                                if (readState.hasNext()) {
                                    readState.readNext();
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
                                if (i == 1) {
                                    readState.isMultiplexStderr = false;
                                } else if (i == 2) {
                                    readState.isMultiplexStderr = true;
                                } else if (i != 0) {
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
                                    if (readState.request.repeatReadPredicatePresent()) {
                                        if (readState.checkReadPredicate()) {
                                            readState.shouldReadMore = false;
                                            return;
                                        }
                                    }
                                    if (readState.currentReadChunkSize == 0) {
                                        readState.isChunkSizeStartPart = true;
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
                                if (readState.hasNext()) {
                                    readState.readNext();
                                    if (readState.hasNext()) {
                                        readState.readNext();
                                    }
                                }
                                readState.isChunkSizeEndPart = true;
                            }
                        }
                    }
                }
            } else {
//                if (!readState.shouldReadMore && readState.readBuffer.array()[readState.readBuffer.limit() - 1] == '\n') {
//                    readState.chars.limit(readState.chars.limit() - 1);
//                }
                readState.writeRemainingToBody();
            }
        }
    }

    @SneakyThrows
    private void read(ReadState readState) {
        readState.resultSize = stateSupplier.get().channel.read(readState.readBuffer, 0).get();
        readState.readBuffer.flip();
    }


    @Getter
    private static final class State {

        @NonFinal
        AsynchronousFileChannel channel;
        String socketFile;
        CharsetDecoder decoder;
        CharsetEncoder encoder;
        Semaphore lock;
        AtomicLong lockTime = new AtomicLong();
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
            takeLock(new String(body, StandardCharsets.UTF_8));
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

        @SneakyThrows
        private void takeLock(String request) {
            lock.acquire();
            System.out.println("Took lock for request -> " + request);
            lockTime.set(System.currentTimeMillis());
        }

        public void releaseLock() {
            System.out.println("Releasing lock, `lockTime` -> " + Duration.ofMillis(System.currentTimeMillis() - lockTime.get()));
            lock.release();
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
                         CompletableFuture<Http.RawResponse> response,
                         SimplePipe stdoutPipe,
                         SimplePipe stderrPipe) {
            this.request = request;
            this.response = response;
            this.stdoutPipe = stdoutPipe;
            this.stderrPipe = stderrPipe;
        }

        public ReadState(Request request,
                         CompletableFuture<Http.RawResponse> response) {
            this(
                    request,
                    response,
                    new SimplePipe(4096),
                    new SimplePipe(4096)
            );
        }

        public ReadState(ReadState rs) {
            this(
                    rs.request,
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
                verifyStatus();
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

        private void verifyStatus() {
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
        }

        public int readNext() {
            return readBuffer.get() & 0xFF;
        }

        public boolean hasNext() {
            return readBuffer.hasRemaining();
        }

        public void writeToStdoutBuffer(char ch) {
            stdoutPipe.write(ch);
        }

        public void writeToStdoutBuffer(int i) {
            stdoutPipe.writeToSinkStream(i);
        }

        public void writeToStderrBuffer(char ch) {
            stderrPipe.write(ch);
        }

        public void writeToStderrBuffer(int i) {
            stderrPipe.writeToSinkStream(i);
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

        public void writeRemainingToBody() {
            var chars = new char[readBuffer.remaining()];
            for (var i = 0; i < chars.length; i++) {
                chars[i] = (char) readBuffer.get();
            }
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

        public boolean checkReadPredicate() {
            return request.repeatReadPredicate()
                    .map(predicate -> {
                        try {
                            var lookFor = predicate.lookFor();
                            var stderrWrites = stderrPipe.useWritesCount();
                            if (stderrWrites > 0 && isMultiplexStderr && (lookFor.isOnlyInStdErr() || lookFor.isInBothStd())) {
                                int i;
                                var ss = stderrPipe.sourceStream();
                                var line = new byte[stderrWrites];
                                for (var r = 0; r < stderrWrites; r++) {
                                    i = ss.read();
                                    line[r] = (byte) i;
                                }
                                String string;
                                if (line.length > 2 && line[stderrWrites - 1] == 10 && line[stderrWrites - 2] == 49) {
                                    string = new String(line, 0, stderrWrites - 1, StandardCharsets.UTF_8);
                                } else if (line.length > 2 && line[stderrWrites - 1] == 10) {
                                    string = new String(line, 0, stderrWrites - 1, StandardCharsets.UTF_8);
                                } else {
                                    string = new String(line, StandardCharsets.UTF_8);
                                }
                                return lookFor.predicate().test(string);
                            } else {
                                var stdoutWrites = stdoutPipe.useWritesCount();
                                if (stdoutWrites > 0 && !isMultiplexStderr && (lookFor.isOnlyInStdOut() || lookFor.isInBothStd())) {
                                    int i;
                                    var ss = stdoutPipe.sourceStream();
                                    var line = new byte[stdoutWrites];
                                    for (var r = 0; r < stdoutWrites; r++) {
                                        i = ss.read();
                                        line[r] = (byte) i;
                                    }
                                    String string;
                                    if (line.length > 2 && line[stdoutWrites - 1] == 10 && line[stdoutWrites - 2] == 49) {
                                        string = new String(line, 0, stdoutWrites - 1, StandardCharsets.UTF_8);
                                    } else if (line.length > 2 && line[stdoutWrites - 1] == 10) {
                                        string = new String(line, 0, stdoutWrites - 1, StandardCharsets.UTF_8);
                                    } else {
                                        string = new String(line, StandardCharsets.UTF_8);
                                    }
                                    return lookFor.predicate().test(string);
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            throw Sneaky.rethrow(e);
                        }
                    })
                    .orElse(false);
        }
    }
}
