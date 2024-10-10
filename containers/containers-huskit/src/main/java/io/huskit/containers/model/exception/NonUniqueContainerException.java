package io.huskit.containers.model.exception;

import io.huskit.containers.model.ContainerType;

public final class NonUniqueContainerException extends RuntimeException {

    public NonUniqueContainerException(String containerId, ContainerType containerType) {
        super(String.format(
                "Container with id %s and type %s is added multiple times. Consider changing ID or removing duplicated container",
                containerId, containerType));
    }
}
