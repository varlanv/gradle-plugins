package io.huskit.containers.http;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.HtLogs;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

final class HttpLogs implements HtLogs {

    HtHttpDockerSpec dockerSpec;
    String containerId;

    public HttpLogs(HtHttpDockerSpec dockerSpec, CharSequence containerId) {
        this.dockerSpec = dockerSpec;
        this.containerId = containerId.toString();
    }

    @Override
    public MultiplexedFrames frames() {
        return asyncFrames().join();
    }

    @Override
    public CompletableFuture<MultiplexedFrames> asyncFrames() {
        return asyncStreamOpen();
    }

    @Override
    public Stream<String> stdOut() {
        return asyncStdOut().join();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdOut() {
        return asyncStreamOpen()
            .thenApply(
                logs -> logs.list().stream()
                    .filter(frame -> frame.type() == FrameType.STDOUT)
                    .map(MultiplexedFrame::stringData)
            );
    }

    @Override
    public Stream<String> stdErr() {
        return asyncStdErr().join();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdErr() {
        return asyncStreamOpen()
            .thenApply(
                logs -> logs.list().stream()
                    .filter(frame -> frame.type() == FrameType.STDERR)
                    .map(MultiplexedFrame::stringData)
            );
    }

    @Override
    public HtFollowedLogs follow() {
        return new HttpFollowedLogs(
            dockerSpec,
            new HttpLogsSpec(containerId).withFollow(true)
        );
    }

    private CompletableFuture<MultiplexedFrames> asyncStreamOpen() {
        return dockerSpec.socket()
            .sendPushAsync(
                new PushRequest<>(
                    new Request(
                        dockerSpec.requests().get(
                            new HttpLogsSpec(containerId)
                        )
                    ).withExpectedStatus(200),
                    new PushMultiplexedStream(
                        StreamType.ALL
                    )
                )
            ).thenApply(
                response -> response.body().value()
            );
    }
}
