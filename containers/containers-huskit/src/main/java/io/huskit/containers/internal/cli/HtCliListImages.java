package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.image.DefHtImageView;
import io.huskit.containers.api.image.HtImageView;
import io.huskit.containers.api.image.HtListImages;
import io.huskit.containers.api.image.MapHtImageRichView;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliListImages implements HtListImages {

    HtCli cli;
    HtCliDckrSpec dockerSpec;
    @With
    HtCliListImagesSpec spec;

    @Override
    public Stream<HtImageView> stream() {
        var commandResult = cli.sendCommand(
                new CliCommand(CommandType.IMAGES_LIST, spec.toCommand()),
                Function.identity()
        );
        return commandResult.lines().stream()
                .map(id -> new DefHtImageView(
                        id,
                        () -> new MapHtImageRichView(
                                cli.sendCommand(
                                        new CliCommand(
                                                CommandType.IMAGES_INSPECT,
                                                List.of("docker", "image", "inspect", "--format=\"{{json .}}\"", id)
                                        ),
                                        (CommandResult inspectCommandResult) -> HtJson.toMap(inspectCommandResult.singleLine())
                                )
                        ))
                );
    }
}
