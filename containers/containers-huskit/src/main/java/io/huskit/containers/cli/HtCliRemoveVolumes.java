package io.huskit.containers.cli;

import io.huskit.containers.api.volume.HtRemoveVolumes;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HtCliRemoveVolumes implements HtRemoveVolumes {

    HtCli cli;
    HtCliRemoveVolumesSpec spec;

    @Override
    public void exec() {
        cli.sendCommand(
            new CliCommand(
                CommandType.VOLUMES_REMOVE,
                spec.toCommand()
            )
        );
    }
}
