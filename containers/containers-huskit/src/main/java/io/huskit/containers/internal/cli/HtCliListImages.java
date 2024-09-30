package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.image.HtImageView;
import io.huskit.containers.api.image.HtListImages;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliListImages implements HtListImages {

    HtCli cli;
    HtCliDckrSpec dockerSpec;
    @With
    HtCliListImagesSpec spec;

    @Override
    public HtCliListImages withAll(Boolean isAll) {
        return this.withSpec(spec.addArg("--all"));
    }

    @Override
    public HtCliListImageFilter filter() {
        return new HtCliListImageFilter(this, spec);
    }

    @Override
    public Stream<HtImageView> stream() {
        var commandResult = cli.sendCommand(
                new CliCommand(CommandType.IMAGES, spec.toCommand()),
                Function.identity()
        );
        return Stream.empty();
    }
}
