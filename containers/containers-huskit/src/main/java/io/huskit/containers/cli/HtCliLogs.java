package io.huskit.containers.cli;

import io.huskit.common.function.CloseableAccessor;
import io.huskit.common.function.ThrowingConsumer;
import io.huskit.common.function.ThrowingFunction;
import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.HtLogs;
import io.huskit.containers.api.container.logs.Logs;
import io.huskit.containers.api.container.logs.LookFor;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliLogs implements HtLogs {

    HtCli cli;
    String id;

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


    @Override
    public CloseableAccessor<Logs> stream() {
        return null;
    }

    @Override
    public CompletableFuture<CloseableAccessor<Logs>> asyncStream() {
        return null;
    }

    @Override
    public CloseableAccessor<Stream<String>> stdOut() {
        return null;
    }

    @Override
    public CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdOut() {
        return null;
    }

    @Override
    public CloseableAccessor<Stream<String>> stdErr() {
        return null;
    }

    @Override
    public CompletableFuture<CloseableAccessor<Stream<String>>> asyncStdErr() {
        return null;
    }

    @Override
    public HtFollowedLogs follow() {
        return new HtCliFollowedLogs(cli, id, LookFor.nothing());
    }
}
