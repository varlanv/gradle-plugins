package io.huskit.containers.internal;

import io.huskit.containers.api.HtContainer;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class HtContainerFromMap implements HtContainer {

    Map<String, Object> source;

    @Override
    public String id() {
        return getFromMap("Id", source);
    }

    @Override
    public String name() {
        return getFromMap("Name", source);
    }

    @Override
    public Map<String, String> labels() {
        Map<String, Object> config = getFromMap("Config", source);
        Map<String, String> labels = getFromMap("Labels", config);
        return Collections.unmodifiableMap(labels);
    }

    @SuppressWarnings({"unchecked"})
    private <T> T getFromMap(String key, Map<String, Object> map) {
        return (T) Optional.ofNullable(map.get(key))
                .orElseThrow(() ->
                        new IllegalStateException(String.format("Could not find key [%s] in container map %s", key, map)));
    }
}
