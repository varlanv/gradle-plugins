package io.huskit.containers.http;

import io.huskit.containers.api.image.*;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
final class HtHttpImages implements HtImages {

    HtHttpDockerSpec spec;

    @Override
    public HtHttpListImages list() {
        return new HtHttpListImages(spec, new HttpListImagesSpec());
    }

    @Override
    public HtHttpListImages list(Consumer<HtListImagesSpec> action) {
        return null;
    }

    @Override
    public HtRmImages rm(CharSequence imageRef) {
        return null;
    }

    @Override
    public HtRmImages rm(CharSequence imageRef, Consumer<HtRmImagesSpec> action) {
        return null;
    }

    @Override
    public <T extends CharSequence> HtRmImages rm(Iterable<T> imageRefs, Consumer<HtRmImagesSpec> action) {
        return null;
    }

    @Override
    public HtPullImages pull(CharSequence imageRef) {
        return new HtHttpPullImages(
            spec,
            new HtHttpPullImagesSpec(
                HtImgName.of(imageRef)
            )
        );
    }

    @Override
    public HtPullImages pull(CharSequence imageRef, Consumer<HtPullImagesSpec> action) {
        var pullSpec = new HtHttpPullImagesSpec(HtImgName.of(imageRef));
        action.accept(pullSpec);
        return new HtHttpPullImages(spec, pullSpec);
    }
}
