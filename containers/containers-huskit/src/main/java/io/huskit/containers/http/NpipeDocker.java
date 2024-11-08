package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.BufferLines;
import lombok.*;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

final class NpipeDocker implements DockerSocket {

    Log log;
    ScheduledExecutorService executor;
    MemoizedSupplier<NpipeChannel> stateSupplier;

    public NpipeDocker(String socketFile, ScheduledExecutorService executor, Log log) {
        this.log = log;
        this.executor = executor;
        this.stateSupplier = MemoizedSupplier.of(() -> new NpipeChannel(socketFile, executor, log, 16384));

    }

    NpipeDocker(String socketFile, ScheduledExecutorService executor) {
        this(socketFile, executor, Log.noop());
    }

    @Override
    public <T> CompletableFuture<Http.Response<T>> sendPushAsync(PushRequest<T> request) {
        return stateSupplier
            .get()
            .writeAndReadAsync(
                new PushRequest<>(
                    request.request(),
                    new HttpPushResponse<>(
                        new PushHead(),
                        request
                    )
                )
            );
    }

    @Override
    public void release() {
        stateSupplier.ifInitialized(NpipeChannel::close);
        executor.shutdownNow();
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
    NpipeChannelIn in;
    NpipeChannelOut out;
    SyncCallback syncCallback;

    NpipeChannel(String socketFile, ScheduledExecutorService executor, Log log, Integer bufferSize) {
        this.socketFile = socketFile;
        this.executor = executor;
        this.channel = openChannel();
        this.syncCallback = new SyncCallback(log);
        this.in = new NpipeChannelIn(
            () -> channel,
            syncCallback,
            log
        );
        this.out = new NpipeChannelOut(
            () -> channel,
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
            (ignore, throwable) -> {
                syncCallback.removeFromQueueAndStartNext(
                    pushRequest.request(),
                    () -> {
                        if (pushRequest.request().dirtiesConnection()) {
                            resetConnection();
                        }
                    }
                );
            }
        );
    }

    @Locked
    @SneakyThrows
    void resetConnection() {
        channel.close();
        channel = openChannel();
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

    Supplier<AsynchronousFileChannel> channel;
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
                log.debug(
                    () -> "Writing to channel: "
                        + System.lineSeparator()
                        + new String(
                        request.http().body(),
                        StandardCharsets.UTF_8
                    )
                );
                channel.get().write(
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
            };
            syncCallback.placeInQueueAndTryStart(request, action);
        }
        return completion;
    }
}

final class NpipeChannelOut {

    Supplier<AsynchronousFileChannel> channel;
    ByteBuffer byteBuffer;
    Log log;

    public NpipeChannelOut(Supplier<AsynchronousFileChannel> channel, Integer bufferSize, Log log) {
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
        channel.get().read(
            byteBuffer.clear(),
            0,
            null,
            new CompletionHandler<>() {

                @Override
                public void completed(Integer result, Object attachment) {
                    byteBuffer.flip();
                    log.debug(
                        () -> "Completed reading from channel: " + System.lineSeparator() + new String(
                            byteBuffer.array(),
                            byteBuffer.position(),
                            byteBuffer.limit(),
                            StandardCharsets.UTF_8
                        )
                    );
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
        channel.get().read(byteBuffer.clear(), 0).get();
        return byteBuffer.flip();
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
                var size = actions.size();
                if (size == 1) {
                    log.debug(() -> "Starting action immediately as it is the only one in the queue");
                } else {
                    log.debug(() -> "Placed action in queue, will wait for " + (size) + " previous actions to finish");
                }
                return size;
            }
        );
        if (actionsSize == 1) {
            action.run();
        }
    }

    void removeFromQueueAndStartNext(Request request, Runnable whenRemovedCallback) {
        sync(
            () -> removeFromQueueAndTakeNext(
                request,
                whenRemovedCallback
            )
        ).run();
    }

    private synchronized <T> T sync(Supplier<T> action) {
        return action.get();
    }

    private Runnable removeFromQueueAndTakeNext(Request request, Runnable whenRemovedCallback) {
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
            log.debug(() -> "No more actions in queue");
            whenRemovedCallback.run();
            return () -> {
            };
        } else {
            actions.remove(indexOfAction);
            log.debug(() -> "Starting next action in queue, " + actions.size() + " actions remaining");
            whenRemovedCallback.run();
            return () -> actions.get(0).action().run();
        }
    }

    @Value
    static class Item {

        Object request;
        Runnable action;
    }
}
