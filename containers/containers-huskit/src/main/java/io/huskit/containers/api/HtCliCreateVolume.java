package io.huskit.containers.api;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.CommandResult;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliCreateVolume implements HtCreateVolume {

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
