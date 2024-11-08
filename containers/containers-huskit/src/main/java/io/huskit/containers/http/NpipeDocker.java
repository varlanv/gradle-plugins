package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.Mutable;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.io.BufferLines;
import io.huskit.common.io.Lines;
import io.huskit.common.number.Hexadecimal;
import io.huskit.containers.internal.HtJson;
import lombok.*;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
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
    @SneakyThrows
    public void release() {
        stateSupplier.ifInitialized(NpipeChannel::close);
        executor.shutdownNow();
    }
}

enum StreamType {
    STDOUT,
    STDERR,
    ALL
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

    static PushResponse<?> ready() {
        return new PushResponse<>() {

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public Object value() {
                return true;
            }

            @Override
            public Optional<Object> apply(ByteBuffer byteBuffer) {
                return Optional.of(true);
            }
        };
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

final class PushJsonArray implements PushResponse<List<Map<String, Object>>> {

    PushResponse<List<Map<String, Object>>> delegate = new PushChunked<>(HtJson::toMapList);

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public List<Map<String, Object>> value() {
        return delegate.value();
    }

    @Override
    public Optional<List<Map<String, Object>>> apply(ByteBuffer byteBuffer) {
        return delegate.apply(byteBuffer);
    }
}

final class PushJsonObject implements PushResponse<Map<String, Object>> {

    PushResponse<Map<String, Object>> delegate = new PushChunked<>(HtJson::toMap);

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public Map<String, Object> value() {
        return delegate.value();
    }

    @Override
    public Optional<Map<String, Object>> apply(ByteBuffer byteBuffer) {
        return delegate.apply(byteBuffer);
    }
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

    PushMultiplexedStream() {
        this(StreamType.ALL);
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
                        } else if (currentByte == 2) {
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
                    if (currentFrameSize > 1) {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize] = currentByte;
                    } else {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize] = currentByte;
                        var frame = new MultiplexedFrame(currentFrameBuffer, currentFrameType);
                        if (streamType == StreamType.ALL || (currentFrameType == FrameType.STDERR && streamType == StreamType.STDERR) ||
                            (currentFrameType == FrameType.STDOUT && streamType == StreamType.STDOUT)) {
                            frameList.add(frame);
                            if (follow.isPresent()) {
                                if (follow.require().test(frame)) {
                                    response.set(new MultiplexedFrames(frameList));
                                    return Optional.of(response.require());
                                } else {
                                    isStreamFrameHeaderPart = true;
                                }
                            } else {
                                response.set(new MultiplexedFrames(frameList));
                                isStreamFrameHeaderPart = true;
                            }
                        }
                    }
                    currentFrameSize--;
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
final class PushChunked<T> implements PushResponse<T> {

    Function<String, T> delegate;
    Mutable<T> body = Mutable.of();
    StringBuilder fullBody = new StringBuilder(256);
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
    public T value() {
        return body.require();
    }

    @Override
    public Optional<T> apply(ByteBuffer byteBuffer) {
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
                        var value = delegate.apply(fullBody.toString());
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

@RequiredArgsConstructor
final class HttpPushResponse<T> implements PushResponse<Http.Response<T>> {

    PushResponse<Http.Head> futureHead;
    PushRequest<T> request;
    Mutable<Http.Response<T>> response = Mutable.of();

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public Http.Response<T> value() {
        return response.require();
    }

    @Override
    public Optional<Http.Response<T>> apply(ByteBuffer byteBuffer) {
        if (futureHead.isReady()) {
            return getHttpResponse(byteBuffer, futureHead.value());
        } else {
            var maybeHead = futureHead.apply(byteBuffer);
            if (maybeHead.isEmpty()) {
                return Optional.empty();
            } else {
                return getHttpResponse(byteBuffer, maybeHead.get());
            }
        }
    }

    private Optional<Http.Response<T>> getHttpResponse(ByteBuffer byteBuffer, Http.Head head) {
        var maybeBody = request.pushResponse().apply(byteBuffer);
        if (maybeBody.isEmpty()) {
            return Optional.empty();
        } else {
            var value = Http.Response.of(head, Http.Body.of(maybeBody.get()));
            response.set(value);
            if (request.request().expectedStatus().isPresent()) {
                if (!Objects.equals(head.status(), request.request().expectedStatus().get().status())) {
                    throw new IllegalStateException(
                        "Expected HTTP status "
                            + request.request().expectedStatus().get().status()
                            + " but got "
                            + head.status()
                            + ": " + value.body().value()
                    );
                }
            }
            return Optional.of(value);
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
