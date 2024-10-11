package io.huskit.containers.cli;

import io.huskit.containers.api.image.HtRmImages;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class HtCliRmImages implements HtRmImages {

    HtCli cli;
    HtCliRmImagesSpec rmImagesSpec;

    @Override
    public void exec() {
        cli.sendCommand(new CliCommand(CommandType.IMAGES_REMOVE, rmImagesSpec.toCommand()));
    }
}
