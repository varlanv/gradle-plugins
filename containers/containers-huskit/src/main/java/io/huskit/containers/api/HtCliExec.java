package io.huskit.containers.api;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.CommandResult;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class HtCliExec implements HtExec {

    HtCli cli;
    HtCliExecSpec spec;

    @Override
    public CommandResult exec() {
        return cli.sendCommand(
                new CliCommand(
                        CommandType.CONTAINERS_EXEC,
                        spec.toCommand()
                ),
                Function.identity()
        );
    }
}
