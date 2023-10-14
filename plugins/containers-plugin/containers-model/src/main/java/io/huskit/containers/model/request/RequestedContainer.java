package io.huskit.containers.model.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.reuse.ContainerReuse;

public interface RequestedContainer {

    ContainerRequestSource source();

    ContainerImage image();

    ContainerPort port();

    ContainerId id();

    ContainerType containerType();

    ContainerReuse containerReuse();
}
