package io.huskit.common.io;

import java.util.Objects;

public final class Line {

    private final String value;

    public Line() {
        this.value = null;
    }

    public Line(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public String value() {
        if (value == null) {
            throw new IllegalStateException("Line not exists");
        }
        return value;
    }

    public Boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public Boolean isNotEmpty() {
        return !isEmpty();
    }

    public Boolean isBlank() {
        return value == null || value.isBlank();
    }

    public Boolean isNotBlank() {
        return !isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
