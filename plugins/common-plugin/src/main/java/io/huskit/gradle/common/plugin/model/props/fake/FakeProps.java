package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.NonNullProp;
import io.huskit.gradle.common.plugin.model.props.NullableProp;
import io.huskit.gradle.common.plugin.model.props.Props;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FakeProps implements Props {

    private final Map<String, Object> props = new HashMap<>();
    private final Map<String, Object> envProps = new HashMap<>();

    @Override
    public boolean hasProp(String name) {
        return props.containsKey(name);
    }

    @Override
    public NonNullProp nonnull(String name) {
        return new FakeNonNullProp(name, props.get(name));
    }

    @Override
    public NullableProp nullable(String name) {
        return new FakeNullableProp(name, props.get(name));
    }

    @Override
    public NullableProp env(String name) {
        return new FakeNullableProp(name, envProps.get(name));
    }

    public void add(String name, Object value) {
        props.put(name, value);
    }

    public void addEnv(String name, String value) {
        envProps.put(name, value);
    }
}
