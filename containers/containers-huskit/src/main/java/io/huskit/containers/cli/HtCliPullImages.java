package io.huskit.containers.cli;

import io.huskit.containers.api.image.HtPullImages;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliPullImages implements HtPullImages {

    HtCli cli;
    HtCliPullImagesSpec pullImagesSpec;

    @Override
    public void exec() {
        cli.sendCommand(new CliCommand(CommandType.IMAGES_PULL, pullImagesSpec.toCommand()));
    }
}
