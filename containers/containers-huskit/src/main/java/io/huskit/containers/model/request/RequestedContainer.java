package io.huskit.containers.model.request;

import io.huskit.common.port.ContainerPort;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerKey;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.reuse.ContainerReuseOptions;

public interface RequestedContainer {

    ContainerRequestSource source();

    ContainerImage image();

    ContainerPort port();

    ContainerKey key();

    ContainerType containerType();

    ContainerReuseOptions reuseOptions();
}
