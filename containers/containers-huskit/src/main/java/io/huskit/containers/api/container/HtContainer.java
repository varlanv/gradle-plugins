package io.huskit.containers.api.container;

import io.huskit.common.port.MappedPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface HtContainer {

    String id();

    String name();

    HtContainerConfig config();

    HtContainerNetworkSettings network();

    Instant createdAt();

    List<String> args();

    String path();

    String processLabel();

    String platform();

    String driver();

    HtContainerGraphDriver graphDriver();

    String hostsPath();

    String hostnamePath();

    Integer restartCount();

    String mountLabel();

    HtContainerState state();

    HtContainerHostConfig hostConfig();

    String resolvConfPath();

    String logPath();

    Integer firstMappedPort();

    List<MappedPort> ports();

    Map<String, Object> toJsonMap();
}
