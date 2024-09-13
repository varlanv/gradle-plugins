package io.huskit.containers.api.logs;

import io.huskit.containers.api.HtLogs;
import io.huskit.containers.cli.CliCommand;
import io.huskit.containers.cli.CommandResult;
import io.huskit.containers.cli.HtCli;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliLogs implements HtLogs {

    HtCli cli;
    String id;
    LookFor lookFor;

    @Override
    public Stream<String> stream() {
        return cli.sendCommand(
                new CliCommand(List.of("docker", "logs", "-f", id))
                        .withTerminatePredicate(line -> !Objects.equals(lookFor, LookFor.nothing()) && line.contains(lookFor.value())),
                CommandResult::lines
        ).stream();
    }

    @Override
    public HtLogs lookFor(LookFor lookFor) {
        return withLookFor(lookFor);
    }
}