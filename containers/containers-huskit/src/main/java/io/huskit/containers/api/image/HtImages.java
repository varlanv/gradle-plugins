package io.huskit.containers.api.image;

import io.huskit.containers.cli.HtListImagesSpec;

import java.util.function.Consumer;

public interface HtImages {

    HtListImages list();

    HtListImages list(Consumer<HtListImagesSpec> action);

    HtRmImages rm(CharSequence imageRef);

    HtRmImages rm(CharSequence imageRef, Consumer<HtRmImagesSpec> action);

    <T extends CharSequence> HtRmImages rm(Iterable<T> imageRefs, Consumer<HtRmImagesSpec> action);

    HtPullImages pull(CharSequence imageRef);

    HtPullImages pull(CharSequence imageRef, Consumer<HtPullImagesSpec> action);
}
