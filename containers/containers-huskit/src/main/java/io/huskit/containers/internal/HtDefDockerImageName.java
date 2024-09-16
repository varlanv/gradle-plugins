package io.huskit.containers.internal;

import io.huskit.containers.api.HtDockerImageName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HtDefDockerImageName implements HtDockerImageName {

    String id;

    @Override
    public String name() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String tag() {
        throw new UnsupportedOperationException();
    }
}
