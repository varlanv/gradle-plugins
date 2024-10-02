package io.huskit.containers.api;

import java.util.Map;

public interface HtCreateVolumeSpec {

    HtCreateVolumeSpec withDriver(CharSequence driver);

    HtCreateVolumeSpec withDriverOpts(CharSequence driverOptsKey, CharSequence driverOptsValue);

    HtCliCreateVolumeSpec withLabel(CharSequence label);

    HtCliCreateVolumeSpec withLabel(CharSequence key, CharSequence value);

    HtCliCreateVolumeSpec withLabels(Map<String, String> labels);
}
