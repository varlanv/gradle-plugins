package io.huskit.containers.model.started;

import io.huskit.containers.model.request.RequestedContainer;

public interface StartedContainersInternal extends StartedContainers {

    StartedContainerInternal startOrCreateAndStart(RequestedContainer requestedContainer);
}
