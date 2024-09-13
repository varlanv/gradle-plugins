package io.huskit.common;

import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Environment {

    WINDOWS(() -> System.getProperty("os.name").toLowerCase().contains("win")),
    LINUX(() -> System.getProperty("os.name").toLowerCase().contains("win"));

    Supplier<Boolean> predicate;

    private static final Map<Environment, Boolean> IS_CACHE = new EnumMap<>(Map.of(
            WINDOWS, WINDOWS.predicate.get(),
            LINUX, LINUX.predicate.get()
    ));

    public static boolean is(Environment environment) {
        return IS_CACHE.get(environment);
    }
}
