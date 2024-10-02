package io.huskit.containers.api;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface HtVolumes {

    HtListVolumes list();

    HtListVolumes list(Consumer<HtListVolumesSpec> action);

    HtCreateVolume create();

    HtCreateVolume create(CharSequence volumeId);

    HtCreateVolume create(Consumer<HtCreateVolumeSpec> action);

    HtCreateVolume create(CharSequence volumeId, Consumer<HtCreateVolumeSpec> action);

    HtRemoveVolumes rm(CharSequence volumeId, Boolean force);

    <T extends CharSequence> HtRemoveVolumes rm(Iterable<T> volumeIds, Boolean force);

    HtPruneVolumes prune(Consumer<HtPruneVolumesSpec> action);

    HtPruneVolumes prune();

    HtVolumeView inspect(CharSequence volumeId);

    <T extends CharSequence> Stream<HtVolumeView> inspect(Iterable<T> volumeIds);
}
