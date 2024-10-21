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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
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
    MemoizedSupplier<NpipeDocker.State> stateSupplier;

    public NpipeDocker(String socketFile, Log log) {
        this.log = log;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeDocker.State(socketFile, log));

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

    @Getter
    private static final class State {

        @NonFinal
        AsynchronousFileChannel channel;
        Log log;
        String socketFile;
        CharsetDecoder decoder;
        CharsetEncoder encoder;
        Semaphore lock;
        AtomicLong lockTime = new AtomicLong();
        AtomicBoolean isDirtyConnection;

        private State(String socketFile, Log log) {
            this.socketFile = socketFile;
            this.log = log;
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
            takeLock(() -> new String(body, StandardCharsets.UTF_8));
            channel.write(ByteBuffer.wrap(body), 0).get();
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
        private void takeLock(Supplier<String> request) {
            lock.acquire();
            log.debug(() -> "Took lock for request -> " + request);
            lockTime.set(System.currentTimeMillis());
        }

        public void releaseLock() {
            log.debug(() -> "Releasing lock, `lockTime` -> " + Duration.ofMillis(System.currentTimeMillis() - lockTime.get()));
            lock.release();
        }
    }

    @SneakyThrows
    public void release() {
        if (stateSupplier.isInitialized()) {
            stateSupplier.get().channel().close();
        }
    }
}
