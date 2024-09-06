package io.huskit.containers.model.started;

import io.huskit.containers.model.port.ContainerPort;

public interface StartedContainer extends NonStartedContainer, AutoCloseable {

    ContainerPort port();

    NonStartedContainer stop();

    @Override
    default void close() throws Exception {
        stop();
    }
}
