package io.huskit.gradle.common.plugin.model.props;

import org.jetbrains.annotations.Nullable;

public interface Prop {

    String name();

    @Nullable
    Object value();

    @Nullable
    String stringValue();

    default boolean holdsTrue() {
        String value = stringValue();
        return Boolean.TRUE.toString().equalsIgnoreCase(value);
    }

    default boolean holdsFalse() {
        String value = stringValue();
        return Boolean.FALSE.toString().equalsIgnoreCase(value);
    }
}
