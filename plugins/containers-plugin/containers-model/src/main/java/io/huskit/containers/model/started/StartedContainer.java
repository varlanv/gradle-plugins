package io.huskit.containers.model.started;

import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.port.ContainerPort;

public interface StartedContainer extends AutoCloseable {

    ContainerId id();

    ContainerPort port();
}
