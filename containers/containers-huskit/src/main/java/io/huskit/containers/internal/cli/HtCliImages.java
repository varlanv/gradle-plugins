package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.image.HtImages;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class HtCliImages implements HtImages {

    HtCli cli;
    HtCliDckrSpec spec;

    @Override
    public HtCliListImages list() {
        return new HtCliListImages(cli, spec, new HtCliListImagesSpec(List.of()));
    }
}
