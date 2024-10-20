package io.huskit.containers.api.container.logs;

import io.huskit.common.function.CloseableAccessor;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HtLogs {

    CloseableAccessor<Logs> stream();

    CompletableFuture<CloseableAccessor<Logs>> asyncStream();

    CloseableAccessor<Stream<String>> stdOut();

    CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdOut();

    CloseableAccessor<Stream<String>> stdErr();

    CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdErr();

    HtFollowedLogs follow();
}
