package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.rm.HtRm;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class HtCliRm implements HtRm {

    HtCli cli;
    HtCliRmSpec spec;

    @Override
    public void exec() {
        cli.sendCommand(
                new CliCommand(CommandType.REMOVE_CONTAINERS, spec.toCommand()),
                Function.identity()
        );
    }
}
