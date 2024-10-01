package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.image.HtRmImages;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliRmImages implements HtRmImages {

    HtCli cli;
    HtCliRmImagesSpec rmImagesSpec;

    @Override
    public void exec() {
        cli.sendCommand(new CliCommand(CommandType.REMOVE_IMAGES, rmImagesSpec.toCommand()));
    }
}
