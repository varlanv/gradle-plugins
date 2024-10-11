package io.huskit.containers.api.volume;

import java.util.Map;

public interface HtCreateVolumeSpec {

    HtCreateVolumeSpec withDriver(CharSequence driver);

    HtCreateVolumeSpec withDriverOpts(CharSequence driverOptsKey, CharSequence driverOptsValue);

    HtCreateVolumeSpec withLabel(CharSequence label);

    HtCreateVolumeSpec withLabel(CharSequence key, CharSequence value);

    HtCreateVolumeSpec withLabels(Map<String, String> labels);
}
