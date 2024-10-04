package io.huskit.containers.api;

import io.huskit.common.port.MappedPort;

import java.util.List;

public interface HtContainerNetwork {

    List<MappedPort> ports();

    Integer firstMappedPort();
}
