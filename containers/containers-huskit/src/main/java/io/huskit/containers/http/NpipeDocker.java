package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.NoopLog;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.ByteBufferInputStream;
import io.huskit.common.io.FlexCharBuffer;
import io.huskit.common.io.LoopInputStream;
import lombok.Getter;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

final class NpipeDocker implements DockerSocket {

    Log log;
    MemoizedSupplier<NpipeChannel> stateSupplier;

    public NpipeDocker(String socketFile, Log log) {
        this.log = log;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeChannel(socketFile, log, 4096));

    }

    NpipeDocker(String socketFile) {
        this(socketFile, new NoopLog());
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
                log.debug(() -> "todo");
            } finally {
                if (state.isDirtyConnection()) {
                    state.resetConnection();
                }
//                state.releaseLock();
            }
        }).handle((ignore, throwable) -> {
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

final class HeadFromStream implements Http.Head {

    Supplier<Http.Head> headSupplier;

    HeadFromStream(InputStream stream) {
        this.headSupplier = MemoizedSupplier.ofLocal(() -> parse(stream));
    }

    @Override
    public Integer status() {
        return headSupplier.get().status();
    }

    @Override
    public Map<String, String> headers() {
        return headSupplier.get().headers();
    }

    @SneakyThrows
    Http.Head parse(InputStream stream) {
        var char4 = '\0';
        var char3 = '\0';
        var char2 = '\0';
        var char1 = '\0';
        var status = -1;
        var headLineCount = 0;
        var statusReads = -1;
        var isHeaderKeyPart = false;
        var isHeaderValuePart = false;
        var isHeaderValueSkipPart = false;
        var currentHeaderKey = "";
        var buffer = new FlexCharBuffer(512);
        var headers = new HashMap<String, String>();
        while (true) {
            var i = stream.read();
            char1 = (char) i;
            if (i == -1) {
                throw new RuntimeException("Failed to build head with given stream");
            }
            var isCrLf = char1 == '\n' && char2 == '\r';
            if (isCrLf) {
                if (char3 == '\n' && char4 == '\r') {
                    break;
                }
            }
            if (headLineCount == 0) {
                if (statusReads >= 0 && statusReads <= 3) {
                    if (statusReads == 0) {
                        status = (char1 - '0') * 100;
                    } else if (status == 1) {
                        statusReads = status + (char1 - '0') * 10;
                    } else if (status == 2) {
                        statusReads = status + (char1 - '0');
                    }
                    statusReads++;
                } else {
                    if (char1 == ' ' && status == -1) {
                        statusReads++;
                    }
                }
            } else if (isHeaderKeyPart) {
                if (char1 == ':') {
                    isHeaderKeyPart = false;
                    currentHeaderKey = buffer.read();
                    isHeaderValuePart = true;
                    isHeaderValueSkipPart = true;
                } else {
                    if (!isCrLf) {
                        buffer.append(char1);
                    }
                }
            } else if (isHeaderValuePart) {
                if (isHeaderValueSkipPart) {
                    isHeaderValueSkipPart = false;
                } else {
                    if (isCrLf) {
                        var headerVal = buffer.readWithoutLastChar();
                        headers.put(currentHeaderKey, headerVal);
                    } else {
                        buffer.append(char1);
                    }
                }
            }
            if (isCrLf) {
                headLineCount++;
                isHeaderKeyPart = true;
                isHeaderValuePart = false;
                buffer.clear();
            }
            char4 = char3;
            char3 = char2;
            char2 = char1;
        }
        return new DfHead(
                status,
                headers
        );
    }
}

final class NpipeChannel {

    @NonFinal
    @Getter
    volatile AsynchronousFileChannel channel;
    Log log;
    String socketFile;
    AtomicBoolean isDirtyConnection;
    NpipeChannelLock lock;
    Integer bufferSize;

    NpipeChannel(String socketFile, Log log, Integer bufferSize) {
        this.socketFile = socketFile;
        this.log = log;
        this.channel = openChannel();
        this.lock = new NpipeChannelLock(log);
        this.isDirtyConnection = new AtomicBoolean(false);
        this.bufferSize = bufferSize;
    }

    NpipeChannel(Log log, Integer bufferSize) {
        this(
                HtConstants.NPIPE_SOCKET,
                log,
                bufferSize
        );
    }

    CompletableFuture<LoopInputStream> writeAndRead(Request request) {
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
    Integer bufferSize;

    @SneakyThrows
    CompletableFuture<LoopInputStream> read() {
        return CompletableFuture.supplyAsync(() ->
                new LoopInputStream(() ->
                        new ByteBufferInputStream(
                                readToBuffer()
                        )
                )
        );
    }

    private ByteBuffer readToBuffer() throws InterruptedException, ExecutionException {
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
