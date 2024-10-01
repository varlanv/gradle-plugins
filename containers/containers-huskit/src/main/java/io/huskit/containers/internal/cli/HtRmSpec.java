package io.huskit.containers.internal.cli;

public interface HtRmSpec {

    HtRmSpec withForce(Boolean force);

    HtRmSpec withVolumes(Boolean volumes);

    default HtRmSpec withForce() {
        return withForce(true);
    }

    default HtRmSpec withVolumes() {
        return withVolumes(true);
    }
}
