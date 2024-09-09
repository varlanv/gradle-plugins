package io.huskit.containers.model.port;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ResolvedPort implements ContainerPort {

    @Getter
    Integer hostValue;
    Integer containerValue;
    @Getter
    Boolean isFixed;

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }
}
