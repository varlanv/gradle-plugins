package io.huskit.common.io;

import java.util.Objects;

public final class Line {

    int endIndex;
    private final String value;

    public Line() {
        this.value = null;
        this.endIndex = -1;
    }

    public Line(String value, int endIndex) {
        this.value = Objects.requireNonNull(value);
        this.endIndex = endIndex;
    }

    public Line(String value) {
        this(value, -1);
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

    public int endIndex() {
        return endIndex;
    }
}
