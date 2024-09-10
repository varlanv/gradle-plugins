package io.huskit.containers.model.started;

import io.huskit.containers.model.port.ContainerPort;

import java.util.Map;

public interface StartedContainer extends NonStartedContainer, AutoCloseable {

    ContainerPort port();

    NonStartedContainer stop();

    Map<String, String> environment();

    @Override
    default void close() throws Exception {
        stop();
    }
}
