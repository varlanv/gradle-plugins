package io.huskit.containers.api;

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

    String resolvConfPath();

    String logPath();



    Map<String, Object> toJsonMap();
}
