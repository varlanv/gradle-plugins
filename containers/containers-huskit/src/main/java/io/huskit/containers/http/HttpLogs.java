package io.huskit.containers.http;

import io.huskit.common.function.CloseableAccessor;
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
    public CloseableAccessor<Logs> stream() {
        return asyncStream().join();
    }

    @Override
    public CompletableFuture<CloseableAccessor<Logs>> asyncStream() {
        return asyncStreamOpen().thenApply(CloseableAccessor::of);
    }

    @Override
    public CloseableAccessor<Stream<String>> stdOut() {
        return asyncStdOut().join();
    }

    @Override
    public CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdOut() {
        return asyncStreamOpen()
                .thenApply(logs -> CloseableAccessor.of(logs.stdOut(), logs));
    }

    @Override
    public CloseableAccessor<Stream<String>> stdErr() {
        return asyncStdErr().join();
    }

    @Override
    public CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdErr() {
        return asyncStreamOpen()
                .thenApply(logs -> CloseableAccessor.of(logs.stdErr(), logs));
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
                                dockerSpec.requests().get(new HttpLogsSpec(containerId))
                        ).withExpectedStatus(200)
                )
                .thenApply(response ->
                        new Logs.DfLogs(
                                response.stdOutReader(),
                                response.stdErrReader()

                        )
                );
    }
}
