package io.huskit.containers.model.id;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class DefaultContainerId implements ContainerId {

    private final String value;

    public DefaultContainerId(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("{id=%s}", value);
    }
}
