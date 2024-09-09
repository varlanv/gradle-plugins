package io.huskit.containers.model.id;

import io.huskit.common.function.MemoizedSupplier;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@ToString(of = "properties")
@EqualsAndHashCode(of = "properties")
class JsonMapContainerId implements ContainerId {

    NavigableMap<String, Object> properties;
    MemoizedSupplier<String> memoizedSupplier = new MemoizedSupplier<>(this::_json);

    JsonMapContainerId() {
        this(Collections.emptyNavigableMap());
    }

    JsonMapContainerId(Map<String, Object> properties) {
        this(new TreeMap<>(properties));
    }

    @Override
    public String json() {
        return memoizedSupplier.get();
    }

    private String _json() {
        return properties.entrySet().stream()
                .map(entry -> String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public JsonMapContainerId with(String key, Object value) {
        var newProperties = new TreeMap<>(properties);
        newProperties.put(key, value);
        return new JsonMapContainerId(newProperties);
    }

    @Override
    public JsonMapContainerId with(Map<String, Object> map) {
        var newProperties = new TreeMap<>(properties);
        newProperties.putAll(map);
        return new JsonMapContainerId(newProperties);
    }
}
