package io.huskit.containers.api.container.logs;

import io.huskit.containers.http.MultiplexedFrames;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HtFollowedLogs {

    MultiplexedFrames stream();

    CompletableFuture<MultiplexedFrames> streamAsync();

    Stream<String> streamStdOut();

    CompletableFuture<Stream<String>> streamStdOutAsync();

    Stream<String> streamStdErr();

    CompletableFuture<Stream<String>> streamStdErrAsync();

    MultiplexedFrames lookFor(LookFor lookFor);

    CompletableFuture<MultiplexedFrames> lookForAsync(LookFor lookFor);
}
