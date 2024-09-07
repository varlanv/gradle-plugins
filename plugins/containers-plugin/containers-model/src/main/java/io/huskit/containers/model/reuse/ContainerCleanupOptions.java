package io.huskit.containers.model.reuse;

import java.time.Duration;

public interface ContainerCleanupOptions {

    ContainerCleanupOptions NEVER = () -> Duration.ZERO;

    Duration cleanupAfter();

    static ContainerCleanupOptions after(Duration duration) {
        if (duration == null || duration.isZero()) {
            return NEVER;
        }
        return () -> duration;
    }
}
