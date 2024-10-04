package io.huskit.containers.api;

import java.time.Instant;
import java.util.List;

public interface HtContainer {

    String id();

    String name();

    HtContainerConfig config();

    HtContainerNetwork network();

    Instant createdAt();

    List<String> args();

    String path();

    String processLabel();

    String platform();

    String driver();

    String hostsPath();

    String hostnamePath();

    Integer restartCount();

    String mountLabel();

    HtContainerState state();

    String resolvConfPath();

    String logPath();
}
