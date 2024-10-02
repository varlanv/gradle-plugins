package io.huskit.containers.api;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliPruneVolumes implements HtPruneVolumes {

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
