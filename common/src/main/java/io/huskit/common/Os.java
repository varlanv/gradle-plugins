package io.huskit.common;

import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Os {

    WINDOWS("win"),
    MAC("mac"),
    FREEBSD("freebsd"),
    LINUX("linux");

    String pattern;

    private static final Supplier<Os> CURRENT = new MemoizedSupplier<>(Os::initOs);

    public boolean isCurrent() {
        return Objects.equals(this, current());
    }

    private static Os initOs() {
        var osName = System.getProperty("os.name").toLowerCase();
        if (osName == null || osName.isBlank()) {
            throw new IllegalStateException("Cannot determine os name because 'System.getProperty(\"os.name\")' is empty");
        }
        return Arrays.stream(values())
                .filter(os -> osName.contains(os.pattern))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot determine current os"));
    }

    public static Os current() {
        return CURRENT.get();
    }
}
