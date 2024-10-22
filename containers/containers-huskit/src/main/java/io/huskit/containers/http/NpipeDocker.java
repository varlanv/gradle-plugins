package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.NoopLog;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.ByteBufferInputStream;
import io.huskit.common.io.LoopInputStream;
import lombok.Getter;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8), 1024);
        var statusLine = reader.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            throw new RuntimeException("Failed to build head with given stream");
        }
        var statusParts = statusLine.split(" ");
        if (statusParts.length < 2) {
            throw new RuntimeException("Invalid status line: " + statusLine);
        }
        var status = Integer.parseInt(statusParts[1]);
        var headers = new HashMap<String, String>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            var colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                throw new RuntimeException("Invalid header line: " + line);
            }
            var key = line.substring(0, colonIndex).trim();
            var value = line.substring(colonIndex + 1).trim();
            headers.put(key, value);
        }

        return new DfHead(status, headers);
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
