package io.huskit.containers.http;

import io.huskit.common.function.CloseableAccessor;
import io.huskit.common.function.ThrowingFunction;
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

//    @Override
//    public void acceptStream(ThrowingConsumer<Logs> consumer) {
//        asyncStreamOpen().thenAccept(consumer.toUnchecked()).whenComplete(handleStreamClose()).join();
//    }
//
//    @Override
//    public <T> T applyStream(ThrowingFunction<Logs, T> function) {
//        return asyncStreamOpen().thenApply(function.toUnchecked()).whenComplete(handleStreamClose()).join();
//    }
//
//    @Override
//    public CompletableFuture<CloseableAccessor<Logs>> asyncStream() {
//        return asyncStreamOpen().thenApply(CloseableAccessor::of);
//    }
//
//    @Override
//    public void acceptStdOut(ThrowingConsumer<Stream<String>> consumer) {
//        asyncStdOut()
//                .thenAccept(stdOutProducer ->
//                        stdOutProducer.accept(stdOut -> {
//                            consumer.toUnchecked().accept(stdOut);
//                            return null;
//                        }))
//                .join();
//    }
//
//    @Override
//    public <T> T applyStdOut(ThrowingFunction<Stream<String>, T> function) {
//        return asyncStdOut()
//                .thenApply(logs -> {
//
//                })
//                .join();
//    }
//
//    @Override
//    public <T> CompletableFuture<Consumer<Function<Stream<String>, T>>> asyncStdOut() {
//        CompletableFuture<Consumer<Function<Stream<String>, T>>> consumerCompletableFuture = asyncStreamOpen().thenApply(logs -> fn -> applyLogs(logs, logs.stdOut(), fn));
//        return consumerCompletableFuture;
//    }
//
//    @Override
//    public void acceptStdErr(ThrowingConsumer<Stream<String>> consumer) {
//        asyncStdErr().thenAccept(consumer.toUnchecked()).join();
//    }
//
//    @Override
//    public <T> T applyStdErr(ThrowingFunction<Stream<String>, T> function) {
//        return asyncStdErr().thenApply(function.toUnchecked()).join();
//    }
//
//    @Override
//    public <T> CompletableFuture<Consumer<Function<Stream<String>, T>>> asyncStdErr() {
//        return asyncStreamOpen().thenApply(logs -> fn -> applyLogs(logs, logs.stdErr(), fn));
//    }

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
                        new Request<>(
                                dockerSpec.requests().get(new HttpLogsSpec(containerId)),
                                mapLogStreams()
                        ).withExpectedStatus(200)
                )
                .thenApply(response -> response.body().value());
    }

    private ThrowingFunction<Npipe.HttpFlow, Logs> mapLogStreams() {
        return response -> new Logs.DfLogs(
                response.stdOut(),
                response.stdErr()
        );
    }
}
