package io.huskit.gradle.containers.plugin.api;

import io.huskit.gradle.containers.plugin.internal.spec.CleanupSpec;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

public interface CleanupSpecView {

    void after(long time, ChronoUnit unit);

    default void after(long time, String unit) {
        ChronoUnit chronoUnit;
        try {
            chronoUnit = ChronoUnit.valueOf(unit.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Unavailable unit value - '%s', consider using one of available values - %s",
                    unit,
                    CleanupSpec.ALLOWED_UNITS.stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toList())));
        }
        after(time, chronoUnit);
    }

    default void after(Duration duration) {
        after(duration.toMillis(), ChronoUnit.MILLIS);
    }

    default void never() {
        after(0, ChronoUnit.SECONDS);
    }
}
