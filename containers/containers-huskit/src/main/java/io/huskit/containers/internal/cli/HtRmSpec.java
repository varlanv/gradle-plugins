package io.huskit.containers.internal.cli;

public interface HtRmSpec {

    HtRmSpec withForce(Boolean force);

    default HtRmSpec withForce() {
        return withForce(true);
    }

    HtRmSpec withVolumes(Boolean volumes);

    default HtRmSpec withVolumes() {
        return withVolumes(true);
    }
}
