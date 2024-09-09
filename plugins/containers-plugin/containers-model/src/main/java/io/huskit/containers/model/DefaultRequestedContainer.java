package io.huskit.containers.model;

import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.request.ContainerRequestSource;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.reuse.ContainerReuseOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class DefaultRequestedContainer implements RequestedContainer {

    ContainerRequestSource source;
    ContainerImage image;
    ContainerId id;
    ContainerPort port;
    ContainerType containerType;
    ContainerReuseOptions reuseOptions;
}
