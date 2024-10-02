package io.huskit.containers.api.image;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.HtCli;
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
