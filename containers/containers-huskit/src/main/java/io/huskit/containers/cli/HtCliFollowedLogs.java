package io.huskit.containers.cli;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.LookFor;
import io.huskit.containers.http.MultiplexedFrames;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliFollowedLogs implements HtFollowedLogs {

    HtCli cli;
    String id;
    LookFor lookFor;

//    @Override
//    public Logs stream() {
//        return null;
//    }
//
//    @Override
//    public CompletableFuture<Logs> streamAsync() {
//        return null;
//    }

    @Override
    public MultiplexedFrames stream() {
        return null;
    }

    @Override
    public CompletableFuture<MultiplexedFrames> streamAsync() {
        return null;
    }

    @Override
    public Stream<String> streamStdOut() {
        return Stream.empty();
    }

    @Override
    public CompletableFuture<Stream<String>> streamStdOutAsync() {
        return null;
    }

    @Override
    public Stream<String> streamStdErr() {
        return Stream.empty();
    }

    @Override
    public CompletableFuture<Stream<String>> streamStdErrAsync() {
        return null;
    }

    @Override
    public MultiplexedFrames lookFor(LookFor lookFor) {
        return null;
    }

    @Override
    public CompletableFuture<MultiplexedFrames> lookForAsync(LookFor lookFor) {
        return null;
    }

//    @Override
//    public Stream<String> stream() {
//        return cli.sendCommand(
//                new CliCommand(
//                        CommandType.CONTAINERS_LOGS_FOLLOW,
//                        List.of("docker", "logs", "-f", id)
//                ).withTerminatePredicate(line -> !Objects.equals(lookFor, LookFor.nothing()) && lookFor.predicate().test(line))
//                        .withTimeout(lookFor.timeout()),
//                CommandResult::lines
//        ).stream();
//    }
//
//    @Override
//    public HtCliFollowedLogs lookFor(LookFor lookFor) {
//        return this.withLookFor(lookFor);
//    }
}
