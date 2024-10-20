package io.huskit.containers.api.container.logs;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HtFollowedLogs {

    Logs stream();

    CompletableFuture<Logs> streamAsync();

    Stream<String> streamStdOut();

    CompletableFuture<Stream<String>> streamStdOutAsync();

    Stream<String> streamStdErr();

    CompletableFuture<Stream<String>> streamStdErrAsync();

    void lookFor(LookFor lookFor);
}
