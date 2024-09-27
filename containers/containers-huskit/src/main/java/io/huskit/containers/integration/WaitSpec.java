package io.huskit.containers.integration;

import java.time.Duration;

public interface WaitSpec {

    ContainerSpec forLogMessageContaining(CharSequence text, Duration timeout);

    default ContainerSpec forLogMessageContaining(CharSequence text) {
        return this.forLogMessageContaining(text, Duration.ofMinutes(2));
    }
}
