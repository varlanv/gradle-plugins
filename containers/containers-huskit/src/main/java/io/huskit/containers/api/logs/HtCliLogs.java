package io.huskit.containers.api.logs;

import io.huskit.containers.api.HtFollowedLogs;
import io.huskit.containers.api.HtLogs;
import io.huskit.containers.cli.CliCommand;
import io.huskit.containers.cli.CommandResult;
import io.huskit.containers.cli.CommandType;
import io.huskit.containers.cli.HtCli;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliLogs implements HtLogs {

    HtCli cli;
    String id;

    @Override
    public Stream<String> stream() {
        return Stream.of("")
                .flatMap(ignore -> cli.sendCommand(
                        new CliCommand(
                                CommandType.LOGS,
                                List.of("docker", "logs", id)
                        ),
                        CommandResult::lines
                ).stream());
    }

    @Override
    public HtFollowedLogs follow() {
        return new HtCliFollowedLogs(cli, id, LookFor.nothing());
    }
}
