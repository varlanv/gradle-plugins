package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.rm.HtRm;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.function.Function;

@RequiredArgsConstructor
public class HtCliRm implements HtRm {

    HtCli cli;
    @With
    HtCliRmSpec spec;

    @Override
    public HtRm withForce(Boolean force) {
        return this.withSpec(
                spec.withForce(force)
        );
    }

    @Override
    public HtRm withVolumes(Boolean volumes) {
        return this.withSpec(
                spec.withVolumes(volumes)
        );
    }

    @Override
    public void exec() {
        cli.sendCommand(
                new CliCommand(CommandType.REMOVE_CONTAINERS, spec.build()),
                Function.identity()
        );
    }
}
