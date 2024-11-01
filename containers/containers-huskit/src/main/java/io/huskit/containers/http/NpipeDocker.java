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
    Executor executor;
    MemoizedSupplier<NpipeChannel> stateSupplier;

    public NpipeDocker(String socketFile, Executor executor, Log log) {
        this.log = log;
        this.executor = executor;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeChannel(socketFile, executor, log, 4096));

    }

    NpipeDocker(String socketFile, Executor executor) {
        this(socketFile, executor, new NoopLog());
    }

    public NpipeDocker(Executor executor) {
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

final class NpipeChannel {

    @NonFinal
    @Getter
    volatile AsynchronousFileChannel channel;
    String socketFile;
    Executor executor;
    AtomicBoolean isDirtyConnection;
    NpipeChannelLock lock;
    Integer bufferSize;

    NpipeChannel(String socketFile, Executor executor, Log log, Integer bufferSize) {
        this.socketFile = socketFile;
        this.executor = executor;
        this.channel = openChannel();
        this.lock = new NpipeChannelLock(log);
        this.isDirtyConnection = new AtomicBoolean(false);
        this.bufferSize = bufferSize;
    }

    NpipeChannel(Log log, Executor executor, Integer bufferSize) {
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
                        executor,
                        bufferSize
                ).read()
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
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
        );
    }
}

@RequiredArgsConstructor
final class NpipeChannelIn {

    AsynchronousFileChannel channel;
    AtomicBoolean isDirtyConnection;
    NpipeChannelLock lock;

    CompletableFuture<Integer> write(Request request) {
        var body = request.http().body();
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
            if (request.repeatReadPredicate().isPresent()) {
                isDirtyConnection.set(true);
            }
        }
        return completion;
    }
}

@RequiredArgsConstructor
final class NpipeChannelOut {

    AsynchronousFileChannel channel;
    Executor executor;
    Integer bufferSize;

    @SneakyThrows
    CompletableFuture<BufferLines> read() {
        return CompletableFuture.supplyAsync(
                () -> new BufferLines(
                        () -> {
                            var buffer = readToBuffer();
                            return Arrays.copyOf(buffer.array(), buffer.limit());
                        }
                ),
                executor
        );
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
