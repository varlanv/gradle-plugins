package io.huskit.containers.cli;

import io.huskit.containers.api.container.exec.HtExec;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
class HtCliExec implements HtExec {

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
