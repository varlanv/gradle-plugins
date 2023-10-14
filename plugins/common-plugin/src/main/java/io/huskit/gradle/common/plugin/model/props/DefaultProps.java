package io.huskit.gradle.common.plugin.model.props;

import lombok.RequiredArgsConstructor;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.ProviderFactory;

@RequiredArgsConstructor
public class DefaultProps implements Props {

    private final ProviderFactory providers;
    private final ExtraPropertiesExtension extraPropertiesExtension;

    @Override
    public boolean hasProp(String name) {
        return nullable(name).value() != null;
    }

    @Override
    public NonNullProp nonnull(String name) {
        return new DefaultNonNullProp(nullable(name));
    }

    @Override
    public NullableProp nullable(String name) {
        return new DefaultNullableProp(
                name,
                providers.gradleProperty(name).orElse(
                        providers.provider(() -> {
                            if (extraPropertiesExtension.has(name)) {
                                return extraPropertiesExtension.get(name);
                            } else {
                                return null;
                            }
                        }).map(Object::toString))
        );
    }

    @Override
    public NullableProp env(String name) {
        return new DefaultNullableProp(
                name,
                providers.environmentVariable(name)
        );
    }
}
