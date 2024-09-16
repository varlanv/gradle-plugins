package io.huskit.containers.api.logs;

import io.huskit.containers.api.CommandType;
import io.huskit.containers.api.HtFollowedLogs;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.CommandResult;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliFollowedLogs implements HtFollowedLogs {

    HtCli cli;
    String id;
    LookFor lookFor;

    @Override
    public Stream<String> stream() {
        return cli.sendCommand(
                new CliCommand(
                        CommandType.LOGS_FOLLOW,
                        List.of("docker", "logs", "-f", id)
                ).withTerminatePredicate(line -> !Objects.equals(lookFor, LookFor.nothing()) && lookFor.predicate().test(line))
                        .withTimeout(lookFor.timeout()),
                CommandResult::lines
        ).stream();
    }

    @Override
    public HtCliFollowedLogs lookFor(LookFor lookFor) {
        return this.withLookFor(lookFor);
    }
}
