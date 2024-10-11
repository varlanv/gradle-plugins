package io.huskit.containers.cli;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.LookFor;
import io.huskit.containers.model.CommandType;
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
                        CommandType.CONTAINERS_LOGS_FOLLOW,
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
