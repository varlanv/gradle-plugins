package io.huskit.containers.model;

import io.huskit.containers.model.id.ContainerKey;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ContainerLabels {

    private final ContainerKey key;

    public Map<String, String> asMap() {
        return Map.of(
                Constants.KEY_LABEL, key.json(),
                "huskit_container", "true"
        );
    }
}
