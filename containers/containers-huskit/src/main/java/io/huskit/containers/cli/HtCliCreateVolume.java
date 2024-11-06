package io.huskit.containers.cli;

import io.huskit.containers.api.volume.HtCreateVolume;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HtCliCreateVolume implements HtCreateVolume {

    HtCli cli;
    HtCliCreateVolumeSpec spec;

    @Override
    public String exec() {
        return cli.sendCommand(
            new CliCommand(
                CommandType.VOLUMES_CREATE,
                spec.toCommand()
            ),
            CommandResult::singleLine
        );
    }
}
