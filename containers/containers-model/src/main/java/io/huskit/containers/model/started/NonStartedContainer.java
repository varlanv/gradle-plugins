package io.huskit.containers.model.started;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerKey;

public interface NonStartedContainer {

    ContainerKey id();

    ContainerType type();

    StartedContainer start();
}
