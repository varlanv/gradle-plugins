package io.huskit.containers.api.image;

import io.huskit.containers.internal.cli.HtCliRmImages;
import io.huskit.containers.internal.cli.HtListImagesSpec;

import java.util.Collection;
import java.util.function.Consumer;

public interface HtImages {

    HtListImages list();

    HtListImages list(Consumer<HtListImagesSpec> action);

    HtRmImages rm(CharSequence imageId);

    HtRmImages rm(CharSequence imageId, Consumer<HtRmImagesSpec> action);

    <T extends CharSequence> HtCliRmImages rm(Collection<T> imageIds, Consumer<HtRmImagesSpec> action);
}
