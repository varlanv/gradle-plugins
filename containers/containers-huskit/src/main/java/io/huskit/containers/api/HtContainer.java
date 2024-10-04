package io.huskit.containers.api;

import java.time.Instant;

public interface HtContainer {

    String id();

    String name();

    HtContainerConfig config();

    HtContainerNetwork network();

    Instant createdAt();
}
