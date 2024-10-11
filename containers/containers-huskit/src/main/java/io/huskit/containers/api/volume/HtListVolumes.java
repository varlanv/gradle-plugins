package io.huskit.containers.api.volume;

import java.util.stream.Stream;

public interface HtListVolumes {

    Stream<HtVolumeView> stream();
}
