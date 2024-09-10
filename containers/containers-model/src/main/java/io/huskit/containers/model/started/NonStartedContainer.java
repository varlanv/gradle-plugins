package io.huskit.containers.model.started;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;

public interface NonStartedContainer {

    ContainerId id();

    ContainerType type();

    StartedContainer start();
}
