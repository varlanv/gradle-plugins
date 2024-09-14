package io.huskit.containers.internal;

import io.huskit.containers.api.HtContainer;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class HtContainerFromMap implements HtContainer {

    Map<String, Object> source;

    @Override
    public String id() {
        return source.get("Id").toString();
    }

    @Override
    public String name() {
        return source.get("Name").toString();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, String> labels() {
        var config = (Map) source.get("Config");
        var labels = (Map) config.get("Labels");
        return Collections.unmodifiableMap(labels);
    }
}
