package io.huskit.containers.api.container;

import io.huskit.common.port.MappedPort;

import java.util.List;

public interface HtContainerHostConfig {

    Integer firstMappedPort();

    List<MappedPort> ports();
}
