package io.huskit.containers.cli;

import io.huskit.containers.api.image.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
class HtCliImages implements HtImages {

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
    public HtCliRmImages rm(CharSequence imageRef) {
        return new HtCliRmImages(cli, new HtCliRmImagesSpec(List.of(HtImgName.of(imageRef))));
    }

    @Override
    public HtCliRmImages rm(CharSequence imageRef, Consumer<HtRmImagesSpec> action) {
        var spec = new HtCliRmImagesSpec(List.of(HtImgName.of(imageRef)));
        action.accept(spec);
        return new HtCliRmImages(cli, spec);
    }

    @Override
    public <T extends CharSequence> HtCliRmImages rm(Iterable<T> imageRefs, Consumer<HtRmImagesSpec> action) {
        var spec = new HtCliRmImagesSpec(HtImgName.ofPrefix(dockerSpec.imagePrefix(), imageRefs));
        action.accept(spec);
        return new HtCliRmImages(cli, spec);
    }

    @Override
    public HtPullImages pull(CharSequence imageRef) {
        return new HtCliPullImages(cli, new HtCliPullImagesSpec(HtImgName.ofPrefix(dockerSpec.imagePrefix(), imageRef)));
    }

    @Override
    public HtPullImages pull(CharSequence imageRef, Consumer<HtPullImagesSpec> action) {
        var pullImagesSpec = new HtCliPullImagesSpec(HtImgName.ofPrefix(dockerSpec.imagePrefix(), imageRef));
        action.accept(pullImagesSpec);
        return new HtCliPullImages(cli, pullImagesSpec);
    }
}
