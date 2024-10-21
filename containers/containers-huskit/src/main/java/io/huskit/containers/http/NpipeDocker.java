package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.NoopLog;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.ByteBufferInputStream;
import lombok.Getter;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

final class NpipeDocker implements DockerSocket {

    Log log;
    MemoizedSupplier<NpipeChannel> stateSupplier;

    public NpipeDocker(String socketFile, Log log) {
        this.log = log;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeChannel(socketFile, log));

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
                if (state.isDirtyConnection().get()) {
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

@Getter
final class NpipeChannel {

    @NonFinal
    volatile AsynchronousFileChannel channel;
    Log log;
    String socketFile;
    AtomicBoolean isDirtyConnection;
    NpipeChannelLock lock;
    Integer bufferSize = 4096;

    NpipeChannel(String socketFile, Log log) {
        this.socketFile = socketFile;
        this.log = log;
        this.channel = openChannel();
        this.lock = new NpipeChannelLock(log);
        this.isDirtyConnection = new AtomicBoolean(false);
    }

    public CompletableFuture<InputStream> writeAndRead(Request request) {
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

    @SneakyThrows
    void resetConnection() {
        channel.close();
        channel = openChannel();
        isDirtyConnection().set(false);
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

    @SneakyThrows
    CompletableFuture<Integer> write(Request request) {
        var body = request.http().body();
        lock.acquire(() -> new String(body, StandardCharsets.UTF_8));
        var completion = new CompletableFuture<Integer>();
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
        return completion;
    }
}

@RequiredArgsConstructor
final class NpipeChannelOut {

    AsynchronousFileChannel channel;
    Integer bufferSize;

    CompletableFuture<InputStream> read() {
        var completion = new CompletableFuture<InputStream>();
        var buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer, 0, null, new CompletionHandler<>() {

            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip();
                completion.complete(new ByteBufferInputStream(buffer));
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                completion.completeExceptionally(exc);
            }
        });
        return completion;
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
