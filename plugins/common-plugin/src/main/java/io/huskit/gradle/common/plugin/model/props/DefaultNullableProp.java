package io.huskit.gradle.common.plugin.model.props;

import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.Provider;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class DefaultNullableProp implements NullableProp {

    private final String name;
    private final Provider<String> provider;

    @Override
    public String name() {
        return name;
    }

    @Override
    public @Nullable Object value() {
        return provider.getOrNull();
    }

    @Override
    public @Nullable String stringValue() {
        Object value = value();
        return value == null ? null : value.toString();
    }
}
