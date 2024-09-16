package io.huskit.containers.api;

import io.huskit.containers.internal.HtDefDockerImageName;

public interface HtDockerImageName {

    String id();

    String name();

    String tag();

    static HtDockerImageName of(CharSequence fullName) {
        return new HtDefDockerImageName(fullName.toString());
    }
}
