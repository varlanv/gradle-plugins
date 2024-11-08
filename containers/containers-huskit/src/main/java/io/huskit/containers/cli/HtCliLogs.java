package io.huskit.containers.cli;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.HtLogs;
import io.huskit.containers.api.container.logs.Logs;
import io.huskit.containers.http.MultiplexedFrames;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliLogs implements HtLogs {

    HtCli cli;
    String id;

    @Override
    public MultiplexedFrames frames() {
        return null;
    }

    @Override
    public CompletableFuture<MultiplexedFrames> asyncFrames() {
        return null;
    }

    @Override
    public Stream<String> stdOut() {
        return Stream.empty();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdOut() {
        return null;
    }

    @Override
    public Stream<String> stdErr() {
        return Stream.empty();
    }

    @Override
    public CompletableFuture<Stream<String>> asyncStdErr() {
        return null;
    }

    @Override
    public HtFollowedLogs follow() {
        return null;
    }

//    @Override
//    public Stream<String> stream() {
//        return Stream.of("")
//                .flatMap(ignore -> cli.sendCommand(
//                        new CliCommand(
//                                CommandType.CONTAINERS_LOGS,
//                                List.of("docker", "logs", id)
//                        ),
//                        CommandResult::lines
//                ).stream());
//    }
//
//    @Override
//    public CompletableFuture<Stream<String>> streamAsync() {
//        return CompletableFuture.completedFuture(stream());
//    }
//
//    @Override
//    public Stream<String> streamStdOut() {
//        return Stream.empty();
//    }
//
//    @Override
//    public Stream<String> streamStdErr() {
//        return Stream.empty();
//    }
}
