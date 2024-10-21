package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.NoopLog;
import io.huskit.common.function.MemoizedSupplier;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

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
    AsynchronousFileChannel channel;
    Log log;
    String socketFile;
    Semaphore lock;
    AtomicLong lockTime = new AtomicLong();
    Integer bufferSize = 4096;
    AtomicBoolean isDirtyConnection;

    NpipeChannel(String socketFile, Log log) {
        this.socketFile = socketFile;
        this.log = log;
        this.channel = openChannel();
        this.lock = new Semaphore(1);
        this.isDirtyConnection = new AtomicBoolean(false);
    }

    @SneakyThrows
    CompletableFuture<Integer> write(Request request) {
        var body = request.http().body();
        takeLock(() -> new String(body, StandardCharsets.UTF_8));
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

    CompletableFuture<byte[]> read() {
        var completion = new CompletableFuture<byte[]>();
        var buffer = ByteBuffer.allocate(bufferSize);
        channel.read(buffer, 0, null, new CompletionHandler<>() {

            @Override
            public void completed(Integer result, Object attachment) {
                buffer.flip();
                var bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                completion.complete(bytes);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                completion.completeExceptionally(exc);
            }
        });
        return completion;
    }

    @SneakyThrows
    void resetConnection() {
        channel.close();
        channel = openChannel();
        isDirtyConnection().set(false);
    }

    @SneakyThrows
    void takeLock(Supplier<String> request) {
        lock.acquire();
        log.debug(() -> "Took lock for request -> " + request);
        lockTime.set(System.currentTimeMillis());
    }

    void releaseLock() {
        log.debug(() -> "Releasing lock, `lockTime` -> " + Duration.ofMillis(System.currentTimeMillis() - lockTime.get()));
        lock.release();
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
