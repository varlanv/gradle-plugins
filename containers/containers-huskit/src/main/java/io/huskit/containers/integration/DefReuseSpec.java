package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class DefReuseSpec implements ReuseSpec {

    ContainerSpec parent;
    Mutable<ReuseWithTimeout> value = Mutable.of();

    @Override
    public ContainerSpec disabled() {
        value.set(new ReuseWithTimeout(false, Duration.ZERO));
        return parent;
    }

    @Override
    public ContainerSpec enabledWithCleanupAfter(Duration cleanupAfter) {
        if (cleanupAfter.toSeconds() < 60) {
            throw new IllegalArgumentException("'cleanupAfter' cannot be less than 60 seconds");
        }
        value.set(new ReuseWithTimeout(true, cleanupAfter));
        return parent;
    }
}
