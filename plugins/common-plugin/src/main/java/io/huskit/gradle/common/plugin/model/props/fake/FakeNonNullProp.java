package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.NonNullProp;
import io.huskit.gradle.common.plugin.model.props.exception.NonNullPropertyException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class FakeNonNullProp implements NonNullProp {

    private final String name;
    private final Object value;

    @Override
    public String name() {
        return name;
    }

    @Override
    public @Nullable Object value() {
        return Optional.ofNullable(value).orElseThrow(() -> new NonNullPropertyException(name));
    }

    @Override
    public @Nullable String stringValue() {
        return Objects.requireNonNull(value()).toString();
    }
}
