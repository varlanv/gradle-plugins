package io.huskit.containers.model.exception;

import io.huskit.containers.model.ContainerType;

public final class UnknownContainerTypeException extends RuntimeException {

    public UnknownContainerTypeException(ContainerType containerType) {
        super(String.format("Unknown requested container type - [%s]", containerType));
    }
}
