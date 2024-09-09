package io.huskit.containers.model.port;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public final class FixedContainerPort implements ContainerPort {

    Integer hostValue;
    Integer containerValue;

    @Override
    public Integer hostValue() {
        return hostValue;
    }

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }

    @Override
    public Boolean isFixed() {
        return true;
    }
}
