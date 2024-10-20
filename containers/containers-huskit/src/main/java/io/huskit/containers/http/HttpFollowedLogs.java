package io.huskit.containers.http;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.Logs;
import io.huskit.containers.api.container.logs.LookFor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
final class HttpFollowedLogs implements HtFollowedLogs {

    HtHttpDockerSpec dockerSpec;
    HttpLogsSpec logsSpec;

    @Override
    public Logs stream() {
        return streamAsyncInternal().join();
    }

    @Override
    public CompletableFuture<Logs> streamAsync() {
        return streamAsyncInternal();
    }

    @Override
    public Stream<String> streamStdOut() {
        return this.streamStdOutAsync().join();
    }

    @Override
    public CompletableFuture<Stream<String>> streamStdOutAsync() {
        return this.streamAsync().thenApply(Logs::stdOut);
    }

    @Override
    public Stream<String> streamStdErr() {
        return this.streamStdErrAsync().join();
    }

    @Override
    public CompletableFuture<Stream<String>> streamStdErrAsync() {
        return this.streamAsync().thenApply(Logs::stdErr);
    }

    @Override
    @SneakyThrows
    public void lookFor(LookFor lookFor) {
        var timeout = lookFor.timeout();
        if (timeout.isZero()) {
            streamAsyncInternal(request -> request.withRepeatReadPredicate(lookFor, Duration.ofMillis(10)))
                    .join();
        } else {
            streamAsyncInternal(request -> request.withRepeatReadPredicate(lookFor, Duration.ofMillis(10)))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    private CompletableFuture<Logs> streamAsyncInternal() {
        return this.streamAsyncInternal(Function.identity());
    }

    private CompletableFuture<Logs> streamAsyncInternal(Function<Request, Request> requestAction) {
        return dockerSpec.socket()
                .sendAsync(
                        requestAction.apply(
                                new Request(
                                        dockerSpec.requests().get(logsSpec)
                                ).withExpectedStatus(200)
                        )
                )
                .thenApply(response ->
                        new Logs.DfLogs(
                                response.stdOut(),
                                response.stdErr()
                        )
                );
    }
}
