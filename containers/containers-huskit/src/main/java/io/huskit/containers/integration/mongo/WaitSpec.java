package io.huskit.containers.integration.mongo;

import java.time.Duration;

public interface WaitSpec {

    ContainerSpec forLogMessageContaining(CharSequence text, Duration timeout);
}
