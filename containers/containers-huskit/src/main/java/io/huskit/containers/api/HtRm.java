package io.huskit.containers.api;

public interface HtRm {

    HtRm withForce(Boolean force);

    HtRm withVolumes(Boolean volumes);

    default HtRm withForce() {
        return withForce(true);
    }

    default HtRm withVolumes() {
        return withVolumes(true);
    }

    void exec();
}
