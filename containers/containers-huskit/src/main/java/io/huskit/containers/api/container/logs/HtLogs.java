package io.huskit.containers.api.container.logs;

import io.huskit.containers.http.MultiplexedFrames;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HtLogs {

    MultiplexedFrames frames();

    CompletableFuture<MultiplexedFrames> asyncFrames();

    Stream<String> stdOut();

    CompletableFuture<Stream<String>> asyncStdOut();

    Stream<String> stdErr();

    CompletableFuture<Stream<String>> asyncStdErr();

    HtFollowedLogs follow();
}
