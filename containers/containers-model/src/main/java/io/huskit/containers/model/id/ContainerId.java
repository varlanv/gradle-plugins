package io.huskit.containers.model.id;

import java.util.Map;

public interface ContainerId {

    String json();

    ContainerId with(String key, Object value);

    ContainerId with(Map<String, Object> map);

    static ContainerId of(Map<String, Object> map) {
        return new JsonMapContainerId(map);
    }
}
