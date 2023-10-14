package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class InternalProperties {

    public static final String EXTENSION_NAME = "__huskit_internal_properties__";
    private final Map<String, String> properties = new HashMap<>();
    private final ProviderFactory providers;
    private final ExtraPropertiesExtension extraPropertiesExtension;

    public Provider<String> get(String name) {
        String value = properties.get(name);
        if (value != null) {
            return providers.provider(() -> value);
        } else {
            return providers.gradleProperty(name)
                    .orElse(providers.provider(() -> {
                        if (extraPropertiesExtension.has(name)) {
                            return extraPropertiesExtension.get(name);
                        } else {
                            return null;
                        }
                    }).map(Object::toString));
        }
    }

    public void put(String name, String value) {
        properties.put(name, value);
    }
}
