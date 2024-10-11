package io.huskit.containers.cli;

import io.huskit.containers.api.volume.HtPruneVolumes;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HtCliPruneVolumes implements HtPruneVolumes {

    HtCli cli;
    HtCliPruneVolumesSpec pruneVolumesSpec;

    @Override
    public void exec() {
        cli.sendCommand(
                new CliCommand(
                        CommandType.VOLUMES_PRUNE,
                        pruneVolumesSpec.toCommand()
                )
        );
    }
}
