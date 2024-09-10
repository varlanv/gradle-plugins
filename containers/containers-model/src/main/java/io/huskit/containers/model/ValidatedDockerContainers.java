package io.huskit.containers.model;

import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.started.StartedContainer;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
public final class ValidatedDockerContainers implements Containers {

    Containers delegate;
    List<RequestedContainer> requestedContainers;

    @Override
    public List<StartedContainer> start() {
        var containersIds = new HashSet<>();
        requestedContainers.forEach(requestedContainer -> {
            var containerId = requestedContainer.key().json();
            if (!containersIds.add(containerId)) {
                throw new NonUniqueContainerException(containerId, requestedContainer.containerType());
            }
        });
        return delegate.start();
    }
}
