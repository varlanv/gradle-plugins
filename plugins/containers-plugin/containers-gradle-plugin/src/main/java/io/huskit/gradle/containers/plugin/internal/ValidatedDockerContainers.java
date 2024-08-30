package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.Containers;
import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.StartedContainers;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;

@RequiredArgsConstructor
public class ValidatedDockerContainers implements Containers {

    private final Containers delegate;
    private final RequestedContainers requestedContainers;

    @Override
    public StartedContainers start() {
        var containersIds = new HashSet<>();
        requestedContainers.list().forEach(requestedContainer -> {
            String containerId = requestedContainer.id().value();
            if (!containersIds.add(containerId)) {
                throw new NonUniqueContainerException(containerId, requestedContainer.containerType());
            }
        });
        return delegate.start();
    }
}
