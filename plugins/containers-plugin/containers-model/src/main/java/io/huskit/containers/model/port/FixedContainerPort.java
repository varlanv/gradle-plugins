package io.huskit.containers.model.port;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedContainerPort implements ContainerPort {

    private final int number;

    @Override
    public int number() {
        return number;
    }
}
