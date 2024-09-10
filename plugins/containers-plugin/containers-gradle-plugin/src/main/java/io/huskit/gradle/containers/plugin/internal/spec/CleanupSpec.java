package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.gradle.containers.plugin.api.CleanupSpecView;
import org.gradle.api.provider.Property;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface CleanupSpec extends CleanupSpecView {

    Set<ChronoUnit> ALLOWED_UNITS = Collections.unmodifiableSet(
            new LinkedHashSet<>(
                    List.of(
                            ChronoUnit.NANOS,
                            ChronoUnit.MICROS,
                            ChronoUnit.MILLIS,
                            ChronoUnit.SECONDS,
                            ChronoUnit.MINUTES,
                            ChronoUnit.HOURS,
                            ChronoUnit.DAYS
                    )
            )
    );

    Property<Duration> getCleanupAfter();

    @Override
    default void after(long time, ChronoUnit unit) {
        if (!ALLOWED_UNITS.contains(unit)) {
            throw new IllegalArgumentException(String.format("Unavailable unit value - '%s', consider using one of available values - %s",
                    unit,
                    ALLOWED_UNITS.stream().map(Enum::name).map(String::toLowerCase).collect(Collectors.toList())));
        }
        var duration = Duration.of(time, unit);
        if (duration.isNegative()) {
            throw new IllegalArgumentException(String.format("`cleanupAfter` [%s] cannot be negative", duration));
        } else if (time != 0 && duration.getSeconds() < 60) {
            throw new IllegalArgumentException(String.format("`cleanupAfter` [%s] cannot be less than 60 seconds", duration));
        }
        getCleanupAfter().set(duration);
    }
}
