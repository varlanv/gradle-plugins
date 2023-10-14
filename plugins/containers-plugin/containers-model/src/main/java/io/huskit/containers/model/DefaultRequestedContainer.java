package io.huskit.containers.model;

import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.request.ContainerRequestSource;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.reuse.ContainerReuse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultRequestedContainer implements RequestedContainer {

    private final ContainerRequestSource source;
    private final ContainerImage containerImage;
    private final ContainerId containerId;
    private final ContainerType containerType;
    private final ContainerPort containerPort;
    private final ContainerReuse containerReuse;

    @Override
    public ContainerRequestSource source() {
        return source;
    }

    @Override
    public ContainerImage image() {
        return containerImage;
    }

    @Override
    public ContainerPort port() {
        return containerPort;
    }

    @Override
    public ContainerId id() {
        return containerId;
    }

    @Override
    public ContainerType containerType() {
        return containerType;
    }

    @Override
    public ContainerReuse containerReuse() {
        return containerReuse;
    }
}
