package io.huskit.containers.model;

import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.StartedContainers;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;

@RequiredArgsConstructor
public final class ValidatedDockerContainers implements Containers {

    Containers delegate;
    RequestedContainers requestedContainers;

    @Override
    public StartedContainers start() {
        var containersIds = new HashSet<>();
        requestedContainers.stream().forEach(requestedContainer -> {
            var containerId = requestedContainer.id().json();
            if (!containersIds.add(containerId)) {
                throw new NonUniqueContainerException(containerId, requestedContainer.containerType());
            }
        });
        return delegate.start();
    }
}
