package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.image.*;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HtCliImages implements HtImages {

    HtCli cli;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtCliListImages list() {
        return new HtCliListImages(cli, dockerSpec, new HtCliListImagesSpec());
    }

    @Override
    public HtListImages list(Consumer<HtListImagesSpec> action) {
        var listImagesSpec = new HtCliListImagesSpec();
        action.accept(listImagesSpec);
        return new HtCliListImages(cli, dockerSpec, listImagesSpec);
    }

    @Override
    public HtCliRmImages rm(CharSequence imageId) {
        return new HtCliRmImages(cli, new HtCliRmImagesSpec(List.of(imageId.toString())));
    }

    @Override
    public HtCliRmImages rm(CharSequence imageId, Consumer<HtRmImagesSpec> action) {
        var spec = new HtCliRmImagesSpec(imageId);
        action.accept(spec);
        return new HtCliRmImages(cli, spec);
    }

    @Override
    public <T extends CharSequence> HtCliRmImages rm(Collection<T> imageIds, Consumer<HtRmImagesSpec> action) {
        var spec = new HtCliRmImagesSpec(imageIds);
        action.accept(spec);
        return new HtCliRmImages(cli, spec);
    }

    @Override
    public HtPullImages pull(CharSequence imageId) {
        return new HtCliPullImages(cli, new HtCliPullImagesSpec(imageId));
    }

    @Override
    public HtPullImages pull(CharSequence imageId, Consumer<HtPullImagesSpec> action) {
        var pullImagesSpec = new HtCliPullImagesSpec(imageId);
        action.accept(pullImagesSpec);
        return new HtCliPullImages(cli, pullImagesSpec);
    }
}
