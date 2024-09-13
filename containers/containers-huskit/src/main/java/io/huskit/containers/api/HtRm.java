package io.huskit.containers.api;

public interface HtRm {

    HtRm withForce(Boolean force);

    default HtRm withForce() {
        return withForce(true);
    }

    HtRm withVolumes(Boolean volumes);

    default HtRm withVolumes() {
        return withVolumes(true);
    }

    void exec();
}
