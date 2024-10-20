package io.huskit.containers.http;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.HtLogs;
import io.huskit.containers.api.container.logs.Logs;

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
    public Logs stream() {
        return asyncStream().join();
    }

    @Override
    public CompletableFuture<Logs> asyncStream() {
        return asyncStreamOpen();
    }

    @Override
    public Stream<String> stdOut() {
        return asyncStdOut().join();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdOut() {
        return asyncStreamOpen()
                .thenApply(Logs::stdOut);
    }

    @Override
    public Stream<String> stdErr() {
        return asyncStdErr().join();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdErr() {
        return asyncStreamOpen()
                .thenApply(Logs::stdErr);
    }

    @Override
    public HtFollowedLogs follow() {
        return new HttpFollowedLogs(
                dockerSpec,
                new HttpLogsSpec(containerId).withFollow(true)
        );
    }

    private CompletableFuture<Logs> asyncStreamOpen() {
        return dockerSpec.socket().sendAsync(
                        new Request(
                                dockerSpec.requests().get(
                                        new HttpLogsSpec(containerId)
                                )
                        ).withExpectedStatus(200)
                )
                .thenApply(response ->
                        new Logs.DfLogs(
                                response.stdOut(),
                                response.stdErr()

                        )
                );
    }
}
