package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.containers.plugin.internal.request.ContainerRequestSpec;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
public class DefaultContainerId implements ContainerId {

    ContainerRequestSpec containerRequestForTaskSpec;
    Map<String, Object> properties = new TreeMap<>();
    MemoizedSupplier<String> memoizedSupplier = new MemoizedSupplier<>(this::_json);

    @Override
    public String json() {
        return memoizedSupplier.get();
    }

    private String _json() {
        properties.putIfAbsent("rootProjectName", containerRequestForTaskSpec.getRootProjectName().get());
        properties.putIfAbsent("projectName", containerRequestForTaskSpec.getProjectName().get());
        properties.putIfAbsent("image", containerRequestForTaskSpec.getImage().get());
        return properties.entrySet().stream()
                .map(entry -> String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()))
                .reduce("{", (a, b) -> a + ", " + b) + "}";
    }

    public DefaultContainerId with(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public DefaultContainerId with(Map<String, Object> map) {
        properties.putAll(map);
        return this;
    }

    @Override
    public String toString() {
        return json();
    }
}
