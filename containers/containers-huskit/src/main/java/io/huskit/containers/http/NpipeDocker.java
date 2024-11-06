package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.Mutable;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.BufferLines;
import io.huskit.common.io.Lines;
import io.huskit.common.number.Hexadecimal;
import lombok.*;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class NpipeDocker implements DockerSocket {

    Log log;
    ScheduledExecutorService executor;
    MemoizedSupplier<NpipeChannel> stateSupplier;

    public NpipeDocker(String socketFile, ScheduledExecutorService executor, Log log) {
        this.log = log;
        this.executor = executor;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeChannel(socketFile, executor, log, 4096));

    }

    NpipeDocker(String socketFile, ScheduledExecutorService executor) {
        this(socketFile, executor, Log.noop());
    }

    public NpipeDocker(ScheduledExecutorService executor) {
        this(HtConstants.NPIPE_SOCKET, executor);
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
        CompletableFuture.runAsync(
            () -> {
                try {
                    log.debug(() -> "todo");
                } finally {
                    if (state.isDirtyConnection()) {
                        state.resetConnection();
                    }
                    // state.releaseLock();
                }
            },
            executor
        ).handle(
            (ignore, throwable) -> {
                if (throwable != null) {
                    rawResponse.completeExceptionally(throwable);
                }
                return null;
            });
        return CompletableFuture.completedFuture(rawResponse.join());
    }

    @Override
    @SneakyThrows
    public void release() {
        stateSupplier.ifInitialized(NpipeChannel::close);
    }
}

enum StreamType {
    STDOUT,
    STDERR,
    ALL
}

final class DockerHttpMultiplexedStream {

    ByteFlow byteFlow;
    StreamType streamType;
    Executor executor;

    DockerHttpMultiplexedStream(StreamType streamType, Executor executor, Supplier<ByteBuffer> byteBufferSupplier) {
        this.byteFlow = new ByteFlow(byteBufferSupplier, Duration.ofMillis(30));
        this.streamType = Objects.requireNonNull(streamType);
        this.executor = executor;
    }

    DockerHttpMultiplexedStream(Executor executor, Supplier<ByteBuffer> byteBufferSupplier) {
        this(StreamType.ALL, executor, byteBufferSupplier);
    }

    @SneakyThrows
    MultiplexedResponseFollow get() {
        var queue = new LinkedBlockingQueue<MultiplexedFrame>();
        var latch = new CountDownLatch(1);
        var completion = CompletableFuture.runAsync(
            () -> {
                latch.countDown();
                parse(queue);
            },
            executor
        );
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Failed to start multiplexed stream task in 5 seconds");
        }
        return new DfMultiplexedResponseFollow(queue, completion);
    }

    private void parse(LinkedBlockingQueue<MultiplexedFrame> queue) {
        while (true) {
            FrameType type = null;
            var currentStreamFrameSize = -1;
            byte[] currentFrameBuffer = null;
            var isStreamHeader = true;
            var currentChunkSize = readChunkSize();
            if (currentChunkSize == 0) {
                break;
            }
            while (currentChunkSize >= 0) {
                var currentByte = byteFlow.nextByte();
                if (currentByte == '\r' && isStreamHeader) {
                    byteFlow.nextByte();
                    break;
                } else {
                    if (isStreamHeader) {
                        type = currentByte == 1 ? FrameType.STDOUT : FrameType.STDERR;
                        byteFlow.skip(3);
                        currentStreamFrameSize = Math.toIntExact(byteFlow.nextUnsignedInt());
                        currentFrameBuffer = new byte[currentStreamFrameSize];
                        currentChunkSize -= 8;
                        isStreamHeader = false;
                    } else {
                        currentChunkSize -= currentStreamFrameSize;
                        if ((type == FrameType.STDERR && streamType == StreamType.STDOUT) ||
                            (type == FrameType.STDOUT && streamType == StreamType.STDERR)) {
                            byteFlow.skip(currentStreamFrameSize - 1);
                            currentStreamFrameSize = -1;
                        } else {
                            while (currentStreamFrameSize-- > 0) {
                                currentFrameBuffer[currentFrameBuffer.length - currentStreamFrameSize - 1] = currentByte;
                                if (currentStreamFrameSize == 0) {
                                    break;
                                }
                                currentByte = byteFlow.nextByte();
                            }
                            queue.add(
                                new MultiplexedFrame(
                                    Objects.requireNonNull(currentFrameBuffer),
                                    Objects.requireNonNull(type)
                                )
                            );
                        }
                        isStreamHeader = true;
                    }
                }
            }
        }
    }

    int readChunkSize() {
        var currentChunkSize = Hexadecimal.fromHexChars();
        while (true) {
            var currentByte = byteFlow.nextByte();
            if (currentByte == '\r') {
                byteFlow.nextByte();
                break;
            }
            currentChunkSize = currentChunkSize.withHexChar((char) currentByte);
        }
        return currentChunkSize.intValue();
    }
}

@Getter
@RequiredArgsConstructor
final class MultiplexedFrame {

    byte[] data;
    FrameType type;
}

interface MultiplexedResponseFollow extends AutoCloseable {

    Optional<MultiplexedFrame> nextFrame(Duration timeout);

    default Optional<MultiplexedFrame> nextFrame() {
        return nextFrame(Duration.ofMinutes(5));
    }
}

@RequiredArgsConstructor
final class DfMultiplexedResponseFollow implements MultiplexedResponseFollow {

    @Getter
    BlockingQueue<MultiplexedFrame> frames;
    CompletableFuture<Void> completion;

    @Override
    @SneakyThrows
    public Optional<MultiplexedFrame> nextFrame(Duration timeout) {
        return Optional.ofNullable(
            frames.poll(
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
            )
        );
    }

    @Override
    public void close() throws Exception {
        if (!completion.isDone()) {
            completion.cancel(true);
        }
    }
}

enum FrameType {

    STDOUT,
    STDERR
}

final class HeadFromLines implements Http.Head {

    @NonFinal
    Integer indexOfHeadEnd;
    Lines lines;
    Supplier<Http.Head> headSupplier;

    HeadFromLines(Lines lines) {
        this.lines = lines;
        this.headSupplier = MemoizedSupplier.of(() -> parse(lines));
    }

    @Override
    public Integer status() {
        return headSupplier.get().status();
    }

    @Override
    public Map<String, String> headers() {
        return headSupplier.get().headers();
    }

    public Integer indexOfHeadEnd() {
        if (indexOfHeadEnd == null) {
            parse(lines);
            if (indexOfHeadEnd == null) {
                throw new RuntimeException("Failed to build head with given stream");
            }
        }
        return indexOfHeadEnd;
    }

    @SneakyThrows
    private Http.Head parse(Lines lines) {
        var statusLine = lines.next();
        if (statusLine.isBlank()) {
            throw new RuntimeException("Failed to build head with given stream");
        }
        var statusParts = statusLine.value().split(" ");
        if (statusParts.length < 2) {
            throw new RuntimeException("Invalid status line: " + statusLine);
        }
        var status = Integer.parseInt(statusParts[1]);
        var headers = new HashMap<String, String>();
        var contentTypeHeaderFound = false;
        var transferEncodingHeaderFound = false;
        var isChunked = false;
        var isDockerMultiplex = false;
        while (true) {
            var line = lines.next();
            if (line.isEmpty()) {
                indexOfHeadEnd = line.endIndex();
                break;
            }
            var lineVal = line.value();
            var colonIndex = lineVal.indexOf(':');
            if (colonIndex == -1) {
                throw new RuntimeException("Invalid header line: " + line);
            }
            var key = lineVal.substring(0, colonIndex).trim();
            var value = lineVal.substring(colonIndex + 1).trim();
            if (!contentTypeHeaderFound && "Content-Type".equalsIgnoreCase(key)) {
                contentTypeHeaderFound = true;
                isChunked = "chunked".equalsIgnoreCase(value);
            }
            if (!transferEncodingHeaderFound && "Transfer-Encoding".equalsIgnoreCase(key)) {
                transferEncodingHeaderFound = true;
                isDockerMultiplex = "application/vnd.docker.multiplexed-stream".equalsIgnoreCase(value);
            }
            headers.put(key, value);
        }

        return new DfHead(status, headers, isChunked, isDockerMultiplex);
    }
}

interface PushResponse<T> {

    boolean isReady();

    T value();

    Optional<T> apply(ByteBuffer byteBuffer);

    static <T> PushResponse<MyHttpResponse<T>> http(PushResponse<T> body) {
        return new HttpPushResponse<>(
            new PushHead(),
            body
        );
    }

    static <T> PushResponse<T> fake(Function<ByteBuffer, Optional<T>> action) {
        return new PushResponse<>() {

            @Override
            public boolean isReady() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T value() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<T> apply(ByteBuffer byteBuffer) {
                return action.apply(byteBuffer);
            }
        };
    }
}

@Getter
@RequiredArgsConstructor
final class MultiplexedFrames {

    List<MultiplexedFrame> frames;
}

final class PushMultiplexedStream implements PushResponse<MultiplexedFrames> {

    StreamType streamType;
    List<MultiplexedFrame> frameList = new ArrayList<>();
    Mutable<MultiplexedFrames> response = Mutable.of();
    Mutable<Predicate<MultiplexedFrame>> follow = Mutable.of();
    @NonFinal
    boolean isChunkSizePart = true;
    @NonFinal
    int currentFrameHeadIndex = 0;
    @NonFinal
    int currentFrameSize = 0;
    @NonFinal
    boolean isStreamFrameHeaderPart = true;
    @NonFinal
    byte[] currentFrameBuffer;
    @NonFinal
    FrameType currentFrameType;
    @NonFinal
    Hexadecimal currentChunkSizeHex = Hexadecimal.fromHexChars();
    @NonFinal
    int skipNext = 0;

    PushMultiplexedStream(StreamType streamType, Predicate<MultiplexedFrame> follow) {
        this.streamType = Objects.requireNonNull(streamType);
        this.follow.set(follow);
    }

    PushMultiplexedStream(StreamType streamType) {
        this.streamType = Objects.requireNonNull(streamType);
    }

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public MultiplexedFrames value() {
        return response.require();
    }

    @Override
    public synchronized Optional<MultiplexedFrames> apply(ByteBuffer byteBuffer) {
        if (response.isPresent()) {
            return response.maybe();
        }
        while (byteBuffer.hasRemaining()) {
            var currentByte = byteBuffer.get();
            if (skipNext > 0) {
                skipNext--;
                continue;
            }
            if (isChunkSizePart) {
                if (currentByte != '\r') {
                    if (currentByte == '\n') {
                        isChunkSizePart = false;
                        isStreamFrameHeaderPart = true;
                        if (currentChunkSizeHex.intValue() == 0) {
                            response.set(new MultiplexedFrames(frameList));
                            return Optional.of(response.require());
                        }
                    } else {
                        currentChunkSizeHex = currentChunkSizeHex.withHexChar((char) currentByte);
                    }
                }
            } else {
                if (isStreamFrameHeaderPart) {
                    if (currentFrameHeadIndex == 0) {
                        if (currentByte == 1) {
                            currentFrameType = FrameType.STDOUT;
                        } else if (currentByte == 0) {
                            currentFrameType = FrameType.STDERR;
                        } else {
                            throw new IllegalStateException("Invalid frame type: " + currentByte);
                        }
                        currentFrameHeadIndex++;
                    } else if (currentFrameHeadIndex >= 1 && currentFrameHeadIndex <= 3) {
                        currentFrameHeadIndex++;
                    } else {
                        if (currentFrameHeadIndex == 4) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 24);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 5) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 16);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 6) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 8);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 7) {
                            currentFrameSize |= Math.toIntExact(currentByte & 0xFFL);
                            currentFrameBuffer = new byte[currentFrameSize];
                            currentFrameHeadIndex = 0;
                            isStreamFrameHeaderPart = false;
                        }
                    }
                } else {
                    if (--currentFrameSize != 0) {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize - 1] = currentByte;
                    } else {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize - 1] = currentByte;
                        var frame = new MultiplexedFrame(currentFrameBuffer, currentFrameType);
                        if (streamType == StreamType.ALL || (currentFrameType == FrameType.STDERR && streamType == StreamType.STDERR) ||
                            (currentFrameType == FrameType.STDOUT && streamType == StreamType.STDOUT)) {
                            if (follow.isPresent()) {
                                if (follow.require().test(frame)) {
                                    frameList.add(frame);
                                    response.set(new MultiplexedFrames(frameList));
                                    return Optional.of(response.require());
                                }
                            } else {
                                frameList.add(frame);
                                response.set(new MultiplexedFrames(frameList));
                                isStreamFrameHeaderPart = true;
                            }
                        }
                    }
                }
                currentChunkSizeHex.decrement();
                if (currentChunkSizeHex.intValue() == 0) {
                    isChunkSizePart = true;
                    isStreamFrameHeaderPart = true;
                    skipNext = 2;
                }
            }
        }
        return Optional.empty();
    }
}

@RequiredArgsConstructor
final class PushChunked<T> implements PushResponse<Http.Body<T>> {

    Function<String, T> delegate;
    Mutable<Http.Body<T>> body = Mutable.of();
    @NonFinal
    StringBuilder fullBody = new StringBuilder(64);
    @NonFinal
    int skipNext = 0;
    @NonFinal
    boolean isChunkSizePart = true;
    Hexadecimal currentChunkSizeHex = Hexadecimal.fromHexChars();

    @Override
    public boolean isReady() {
        return body.isPresent();
    }

    @Override
    public Http.Body<T> value() {
        return body.require();
    }

    @Override
    public Optional<Http.Body<T>> apply(ByteBuffer byteBuffer) {
        while (byteBuffer.hasRemaining()) {
            var b = byteBuffer.get();
            var ch = (char) (b & 0xFF);
            if (skipNext > 0) {
                skipNext--;
                continue;
            }
            if (isChunkSizePart) {
                if (ch == '\n') {
                    continue;
                }
                if (ch == '\r') {
                    isChunkSizePart = false;
                    if (currentChunkSizeHex.intValue() == 0) {
                        var value = Http.Body.of(delegate.apply(fullBody.toString()));
                        body.set(value);
                        return Optional.of(value);
                    } else {
                        skipNext = 1;
                    }
                } else {
                    currentChunkSizeHex.withHexChar(ch);
                }
            } else {
                var chunkSize = currentChunkSizeHex.decrement().intValue();
                fullBody.append(ch);
                if (chunkSize == 0) {
                    isChunkSizePart = true;
                    skipNext = 2;
                }
            }
        }
        return Optional.empty();
    }
}

@RequiredArgsConstructor
final class PushBody<T> implements PushResponse<Http.Body<T>> {

    PushResponse<T> delegate;
    Mutable<Http.Body<T>> body;

    @Override
    public boolean isReady() {
        return body.isPresent();
    }

    @Override
    public Http.Body<T> value() {
        return body.require();
    }

    @Override
    public Optional<Http.Body<T>> apply(ByteBuffer byteBuffer) {
        var maybeBody = delegate.apply(byteBuffer);
        if (maybeBody.isPresent()) {
            var value = Http.Body.of(maybeBody.get());
            body.set(value);
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }
}

final class PushHead implements PushResponse<Http.Head> {

    Mutable<Http.Head> head = Mutable.of();
    List<String> lines = new ArrayList<>();
    @NonFinal
    StringBuilder lineBuilder = new StringBuilder(64);
    @NonFinal
    char previousChar;

    @Override
    public boolean isReady() {
        return head.isPresent();
    }

    @Override
    public Http.Head value() {
        return head.require();
    }

    @Override
    public synchronized Optional<Http.Head> apply(ByteBuffer byteBuffer) {
        if (head.isPresent()) {
            return head.maybe();
        }
        while (byteBuffer.hasRemaining()) {
            var currentChar = (char) (byteBuffer.get() & 0xFF);
            if (currentChar == '\n' && previousChar == '\r') {
                if (lineBuilder.length() == 1) {
                    var head = new HeadFromLines(Lines.fromIterable(lines));
                    this.head.set(head);
                    return Optional.of(head);
                } else {
                    lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    lines.add(lineBuilder.toString());
                    lineBuilder = new StringBuilder(64);
                }
            } else {
                lineBuilder.append(currentChar);
            }
            previousChar = currentChar;
        }
        return Optional.empty();
    }
}

@Getter
@RequiredArgsConstructor
final class MyHttpResponse<T> {

    Http.Head head;
    T body;
}

@RequiredArgsConstructor
final class HttpPushResponse<T> implements PushResponse<MyHttpResponse<T>> {

    PushResponse<Http.Head> futureHead;
    PushResponse<T> futureBody;
    Mutable<MyHttpResponse<T>> response = Mutable.of();

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public MyHttpResponse<T> value() {
        return response.require();
    }

    @Override
    public Optional<MyHttpResponse<T>> apply(ByteBuffer byteBuffer) {
        if (futureHead.isReady()) {
            return getMyHttpResponse(byteBuffer, futureHead.value());
        } else {
            var maybeHead = futureHead.apply(byteBuffer);
            if (maybeHead.isEmpty()) {
                return Optional.empty();
            } else {
                return getMyHttpResponse(byteBuffer, maybeHead.get());
            }
        }
    }

    private Optional<MyHttpResponse<T>> getMyHttpResponse(ByteBuffer byteBuffer, Http.Head maybeHead) {
        var maybeBody = futureBody.apply(byteBuffer);
        if (maybeBody.isEmpty()) {
            return Optional.empty();
        } else {
            response.set(new MyHttpResponse<>(maybeHead, maybeBody.get()));
            return Optional.of(response.require());
        }
    }
}

@RequiredArgsConstructor
final class NpipeRead<T> {

    CompletableFuture<T> completion;
    Supplier<CompletableFuture<ByteBuffer>> bytesSupplier;
    ScheduledExecutorService executorService;

    void pushTo(PushResponse<T> action) {
        act(action, completion);
    }

    private void act(PushResponse<T> action,
                     CompletableFuture<T> completion) {
        bytesSupplier.get().thenAccept(
            buffer -> {
                try {
                    action.apply(buffer).ifPresentOrElse(
                        completion::complete,
                        () -> executorService.submit(
                            () -> act(
                                action,
                                completion
                            )
                        )
                    );
                } catch (Exception e) {
                    completion.completeExceptionally(e);
                }
            }
        );
    }
}

final class NpipeChannel implements AutoCloseable {

    @NonFinal
    @Getter
    volatile AsynchronousFileChannel channel;
    String socketFile;
    ScheduledExecutorService executor;
    AtomicBoolean isDirtyConnection;
    NpipeChannelIn in;
    NpipeChannelOut out;
    SyncCallback syncCallback;

    NpipeChannel(String socketFile, ScheduledExecutorService executor, Log log, Integer bufferSize) {
        this.socketFile = socketFile;
        this.executor = executor;
        this.channel = openChannel();
        this.isDirtyConnection = new AtomicBoolean(false);
        this.syncCallback = new SyncCallback(log);
        this.in = new NpipeChannelIn(
            channel,
            isDirtyConnection,
            syncCallback,
            log
        );
        this.out = new NpipeChannelOut(
            channel,
            bufferSize,
            log
        );
    }

    NpipeChannel(Log log, ScheduledExecutorService executor, Integer bufferSize) {
        this(
            HtConstants.NPIPE_SOCKET,
            executor,
            log,
            bufferSize
        );
    }

    CompletableFuture<BufferLines> writeAndRead(Request request) {
        return in.write(request).thenCompose(ignore -> out.read());
    }

    <T> CompletableFuture<T> writeAndReadAsync(PushRequest<T> pushRequest) {
        var completion = new CompletableFuture<T>();
        in.write(pushRequest.request())
            .thenRun(
                () -> new NpipeRead<T>(
                    completion,
                    out::readToBufferAsync,
                    executor
                ).pushTo(pushRequest.pushResponse())
            );
        return completion.whenComplete(
            (ignore, throwable) -> syncCallback.removeFromQueueAndStartNext(pushRequest.request())
        );
    }

    Boolean isDirtyConnection() {
        return isDirtyConnection.get();
    }

    @Locked
    @SneakyThrows
    void resetConnection() {
        channel.close();
        channel = openChannel();
        isDirtyConnection.set(false);
    }

    @SneakyThrows
    private AsynchronousFileChannel openChannel() {
        return AsynchronousFileChannel.open(
            Paths.get(socketFile),
            EnumSet.of(
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
            ),
            executor
        );
    }

    @Override
    public void close() throws Exception {
        var ch = channel;
        if (ch != null) {
            ch.close();
        }
    }
}

@RequiredArgsConstructor
final class NpipeChannelIn {

    AsynchronousFileChannel channel;
    AtomicBoolean isDirtyConnection;
    SyncCallback syncCallback;
    Log log;

    CompletableFuture<Integer> write(Request request) {
        var completion = new CompletableFuture<Integer>();
        if (request.http().body().length == 0) {
            log.error(() -> "Cannot write empty body");
            completion.completeExceptionally(
                new IllegalArgumentException("Cannot write empty body")
            );
        } else {
            Runnable action = () -> {
                channel.write(
                    ByteBuffer.wrap(request.http().body()),
                    0,
                    null,
                    new CompletionHandler<>() {

                        @Override
                        public void completed(Integer result, Object attachment) {
                            completion.complete(result);
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {
                            completion.completeExceptionally(exc);
                        }
                    }
                );
                if (request.repeatReadPredicatePresent()) {
                    isDirtyConnection.set(true);
                }
            };
            syncCallback.placeInQueueAndTryStart(request, action);
        }
        return completion;
    }
}

final class NpipeChannelOut {

    AsynchronousFileChannel channel;
    ByteBuffer byteBuffer;
    Log log;

    public NpipeChannelOut(AsynchronousFileChannel channel, Integer bufferSize, Log log) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        this.channel = channel;
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.log = log;
    }

    @SneakyThrows
    CompletableFuture<BufferLines> read() {
        return CompletableFuture.supplyAsync(
            () -> new BufferLines(
                () -> {
                    var buffer = readToBuffer();
                    return Arrays.copyOf(buffer.array(), buffer.limit());
                }
            )
        );
    }

    CompletableFuture<ByteBuffer> readToBufferAsync() {
        var completion = new CompletableFuture<ByteBuffer>();
        log.debug(() -> "Started reading from channel");
        channel.read(
            byteBuffer.clear(),
            0,
            null,
            new CompletionHandler<>() {

                @Override
                public void completed(Integer result, Object attachment) {
                    log.debug(() -> "Completed reading from channel, `result` -> " + result);
                    byteBuffer.flip();
                    completion.complete(byteBuffer);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    log.error(() -> "Failed to read from channel");
                    completion.completeExceptionally(exc);
                }
            }
        );
        return completion;
    }

    @SneakyThrows
    private ByteBuffer readToBuffer() {
        channel.read(byteBuffer.clear(), 0).get();
        return byteBuffer.flip();
    }
}

@RequiredArgsConstructor
final class NpipeChannelLock {

    Semaphore lock = new Semaphore(1);

    AtomicLong lockTime = new AtomicLong();
    Log log;

    @SneakyThrows
    void acquire(Supplier<String> request) {
        lock.acquire();
        log.debug(() -> "Took lock for request -> " + request.get());
        lockTime.set(System.currentTimeMillis());
    }

    void releaseLock() {
        if (!isAcquired()) {
            throw new IllegalStateException("Trying to release lock that is not acquired");
        }
        log.debug(() -> "Releasing lock, `lockTime` -> " + Duration.ofMillis(System.currentTimeMillis() - lockTime.get()));
        lock.release();
    }

    @VisibleForTesting
    boolean isAcquired() {
        return lock.availablePermits() == 0;
    }
}

final class ByteFlow {

    Supplier<ByteBuffer> byteBufferSupplier;

    @NonFinal
    ByteBuffer currentBuffer;
    Duration pollBackoff;

    ByteFlow(Supplier<ByteBuffer> byteBufferSupplier, Duration pollBackoff) {
        this.byteBufferSupplier = byteBufferSupplier;
        this.currentBuffer = byteBufferSupplier.get();
        this.pollBackoff = pollBackoff;
    }

    @SneakyThrows
    byte nextByte() {
        if (currentBuffer.hasRemaining()) {
            return currentBuffer.get();
        }
        var backoff = pollBackoff.toMillis();
        if (backoff > 0) {
            Thread.sleep(backoff);
        }
        currentBuffer = byteBufferSupplier.get();
        return currentBuffer.get();
    }

    long nextUnsignedInt() {
        return ((nextByte() & 0xFFL) << 24) |
            ((nextByte() & 0xFFL) << 16) |
            ((nextByte() & 0xFFL) << 8) |
            (nextByte() & 0xFFL);
    }

    void skip(int n) {
        for (int i = 0; i < n; i++) {
            nextByte();
        }
    }
}

final class SyncCallback {

    Log log;
    List<Item> actions;

    SyncCallback(Log log) {
        this.log = log;
        this.actions = new ArrayList<>();
    }

    void placeInQueueAndTryStart(Object request, Runnable action) {
        var actionsSize = sync(
            () -> {
                actions.add(
                    new SyncCallback.Item(
                        request,
                        action
                    )
                );
                return actions.size();
            }
        );
        if (actionsSize == 1) {
            log.debug(() -> "Starting action immediately as it is the only one in the queue");
            action.run();
        } else {
            log.debug(() -> "Placed action in queue, will wait for " + (actionsSize - 1) + " previous actions to finish");
        }
    }

    void removeFromQueueAndStartNext(Request request) {
        sync(
            () -> removeFromQueueAndTakeNext(
                request
            )
        ).run();
    }

    private synchronized <T> T sync(Supplier<T> action) {
        return action.get();
    }

    private Runnable removeFromQueueAndTakeNext(Request request) {
        var indexOfAction = -1;
        for (var idx = 0; idx < actions.size(); idx++) {
            var item = actions.get(idx);
            if (item.request() == request) {
                indexOfAction = idx;
                break;
            }
        }
        if (indexOfAction == -1) {
            throw new IllegalStateException("Failed to find action for request");
        }
        if (actions.size() == 1) {
            actions.clear();
            return () -> log.debug(() -> "No more actions in queue");
        } else {
            actions.remove(indexOfAction);
            return () -> {
                log.debug(() -> "Starting next action in queue, " + actions.size() + " actions remaining");
                actions.get(0).action().run();
            };
        }
    }

    @Value
    static class Item {

        Object request;
        Runnable action;
    }
}
