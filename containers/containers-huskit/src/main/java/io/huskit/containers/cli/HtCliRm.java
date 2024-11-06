package io.huskit.containers.cli;

import io.huskit.containers.api.container.rm.HtRm;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
class HtCliRm implements HtRm {

    HtCli cli;
    HtCliRmSpec spec;

    @Override
    public void exec() {
        cli.sendCommand(
            new CliCommand(CommandType.CONTAINERS_REMOVE, spec.toCommand()),
            Function.identity()
        );
    }
}
