package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.NonNullProp;
import io.huskit.gradle.common.plugin.model.props.NullableProp;
import io.huskit.gradle.common.plugin.model.props.Props;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RandomizeIfEmptyProps implements Props {

    private final Props delegate;

    @Override
    public boolean hasProp(String name) {
        return true;
    }

    @Override
    public NonNullProp nonnull(String name) {
        if (!delegate.hasProp(name)) {
            String value = UUID.randomUUID().toString();
            return new FakeNonNullProp(name, value);
        }
        return delegate.nonnull(name);
    }

    @Override
    public NullableProp nullable(String name) {
        if (!delegate.hasProp(name)) {
            String value = UUID.randomUUID().toString();
            return new FakeNullableProp(name, value);
        }
        return delegate.nullable(name);
    }

    @Override
    public NullableProp env(String name) {
        NullableProp env = delegate.env(name);
        if (env.value() == null) {
            return new FakeNullableProp(name, UUID.randomUUID().toString());
        }
        return env;
    }
}
