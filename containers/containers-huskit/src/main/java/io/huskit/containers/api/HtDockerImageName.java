package io.huskit.containers.api;

import io.huskit.containers.internal.HtDefaultDockerImageName;

public interface HtDockerImageName {

    String fullName();

    String id();

    String version();

    static HtDockerImageName of(CharSequence fullName) {
        return new HtDefaultDockerImageName(fullName.toString());
    }
}
