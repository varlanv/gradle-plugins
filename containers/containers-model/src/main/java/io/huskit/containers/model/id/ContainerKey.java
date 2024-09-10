package io.huskit.containers.model.id;

import java.util.Map;

public interface ContainerKey {

    String json();

    ContainerKey with(String key, Object value);

    ContainerKey with(Map<String, Object> map);

    static ContainerKey of(Map<String, Object> map) {
        return new JsonMapContainerKey(map);
    }
}
