package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.NullableProp;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@RequiredArgsConstructor
public class FakeNullableProp implements NullableProp {


    private final String name;
    private final Object value;

    @Override
    public String name() {
        return name;
    }

    @Override
    public @Nullable Object value() {
        return value;
    }

    @Override
    public @Nullable String stringValue() {
        return Objects.toString(value(), null);
    }
}
