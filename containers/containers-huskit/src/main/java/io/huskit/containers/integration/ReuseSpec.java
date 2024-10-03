package io.huskit.containers.integration;

import java.time.Duration;

public interface ReuseSpec {

    ContainerSpec disabled();

    ContainerSpec enabledWithCleanupAfter(Duration cleanupAfter);
}
