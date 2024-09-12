package io.huskit.containers;

import io.huskit.containers.api.HtDockerImageName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class HtDefaultDockerImageName implements HtDockerImageName {

    String fullName;

    @Override
    public String id() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String version() {
        throw new UnsupportedOperationException();
    }
}
