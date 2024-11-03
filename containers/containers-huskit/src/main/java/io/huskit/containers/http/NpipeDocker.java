package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.NoopLog;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.BufferLines;
import io.huskit.common.io.Lines;
import io.huskit.common.number.Hexadecimal;
import lombok.Getter;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
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
        this(socketFile, executor, new NoopLog());
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
        if (stateSupplier.isInitialized()) {
            stateSupplier.get().channel().close();
        }
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
            var framesCounter = 0;
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
                        framesCounter++;
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
    Supplier<Http.Head> headSupplier;

    HeadFromLines(Lines lines) {
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
            headSupplier.get();
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

interface FutureResponse<T> {

    T value();

    boolean apply(ByteBuffer byteBuffer);
}

@RequiredArgsConstructor
final class NpipeRead {

    Supplier<CompletableFuture<ByteBuffer>> bytesSupplier;
    ExecutorService executorService;

    <T> CompletableFuture<T> read(FutureResponse<T> action) {
        var completion = new CompletableFuture<T>();
        act(action, completion);
        return completion;
    }

    private <T> void act(FutureResponse<T> action,
                         CompletableFuture<T> completion) {
        bytesSupplier.get().thenAccept(buffer -> {
            try {
                var apply = action.apply(buffer);
                if (apply) {
                    completion.complete(action.value());
                } else {
                    executorService.submit(() -> act(action, completion));
                }
            } catch (Exception e) {
                completion.completeExceptionally(e);
            }
        });
    }
}

final class NpipeChannel implements AutoCloseable {

    @NonFinal
    @Getter
    volatile AsynchronousFileChannel channel;
    String socketFile;
    ScheduledExecutorService executor;
    AtomicBoolean isDirtyConnection;
    NpipeChannelLock lock;
    Integer bufferSize;

    NpipeChannel(String socketFile, ScheduledExecutorService executor, Log log, Integer bufferSize) {
        this.socketFile = socketFile;
        this.executor = executor;
        this.channel = openChannel();
        this.lock = new NpipeChannelLock(log);
        this.isDirtyConnection = new AtomicBoolean(false);
        this.bufferSize = bufferSize;
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
        return new NpipeChannelIn(
                channel,
                isDirtyConnection,
                lock
        ).write(request).thenCompose(ignore ->
                new NpipeChannelOut(
                        channel,
                        bufferSize
                ).read()
        );
    }

    CompletableFuture<Supplier<CompletableFuture<ByteBuffer>>> writeAndReadAsync(byte[] bytes, Boolean dirtiesConnection) {
        var in = new NpipeChannelIn(
                channel,
                isDirtyConnection,
                lock
        );
        var out = new NpipeChannelOut(
                channel,
                bufferSize
        );
        return in.write(bytes, dirtiesConnection).thenApply(ignore -> out::readToBufferAsync);
    }

    CompletableFuture<Supplier<CompletableFuture<ByteBuffer>>> writeAndReadAsync(byte[] bytes) {
        return writeAndReadAsync(bytes, false);
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
    NpipeChannelLock lock;

    CompletableFuture<Integer> write(Request request) {
        return write(request.http().body(), request.repeatReadPredicatePresent());
    }

    CompletableFuture<Integer> write(byte[] body, Boolean dirtiesConnection) {
        var completion = new CompletableFuture<Integer>();
        if (body.length == 0) {
            completion.completeExceptionally(new IllegalArgumentException("Cannot write empty body"));
        } else {
            lock.acquire(() -> new String(body, StandardCharsets.UTF_8));
            channel.write(ByteBuffer.wrap(body), 0, null, new CompletionHandler<>() {

                @Override
                public void completed(Integer result, Object attachment) {
                    completion.complete(result);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    completion.completeExceptionally(exc);
                }
            });
            if (dirtiesConnection) {
                isDirtyConnection.set(true);
            }
        }
        return completion;
    }
}

final class NpipeChannelOut {

    AsynchronousFileChannel channel;
    Integer bufferSize;

    public NpipeChannelOut(AsynchronousFileChannel channel, Integer bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        this.channel = channel;
        this.bufferSize = bufferSize;
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
        var buffer = ByteBuffer.allocate(bufferSize);
        var completion = new CompletableFuture<ByteBuffer>();
        channel.read(buffer, 0, null, new CompletionHandler<>() {

            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip();
                completion.complete(buffer);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                completion.completeExceptionally(exc);
            }
        });
        return completion;
    }

    @SneakyThrows
    private ByteBuffer readToBuffer() {
        var buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer, 0).get();
        buffer.flip();
        return buffer;
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
