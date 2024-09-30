package io.huskit.containers.api.image;

import java.util.stream.Stream;

public interface HtListImages {

    HtListImages withAll(Boolean isAll);

    HtListImageFilter filter();

    Stream<HtImageView> stream();
}
