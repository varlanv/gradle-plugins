package io.huskit.containers.api.container.logs;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HtLogs {

    Logs stream();

    CompletableFuture<Logs> asyncStream();

    Stream<String> stdOut();

    CompletableFuture<Stream<String>> asyncStdOut();

    Stream<String> stdErr();

    CompletableFuture<Stream<String>> asyncStdErr();

    HtFollowedLogs follow();
}
