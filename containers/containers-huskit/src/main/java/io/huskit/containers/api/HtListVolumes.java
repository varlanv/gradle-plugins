package io.huskit.containers.api;

import java.util.stream.Stream;

public interface HtListVolumes {

    Stream<HtVolumeView> stream();
}
