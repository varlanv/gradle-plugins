package io.huskit.containers.api.image;

import io.huskit.containers.internal.cli.HtCliRmImages;
import io.huskit.containers.internal.cli.HtListImagesSpec;

import java.util.function.Consumer;

public interface HtImages {

    HtListImages list();

    HtListImages list(Consumer<HtListImagesSpec> action);

    HtRmImages rm(CharSequence imageRef);

    HtRmImages rm(CharSequence imageRef, Consumer<HtRmImagesSpec> action);

    <T extends CharSequence> HtCliRmImages rm(Iterable<T> imageRefs, Consumer<HtRmImagesSpec> action);

    HtPullImages pull(CharSequence imageRef);

    HtPullImages pull(CharSequence imageRef, Consumer<HtPullImagesSpec> action);
}
