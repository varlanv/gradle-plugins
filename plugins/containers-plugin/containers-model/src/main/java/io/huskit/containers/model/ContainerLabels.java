package io.huskit.containers.model;

import io.huskit.containers.model.id.ContainerId;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ContainerLabels {

    private final ContainerId id;

    public Map<String, String> asMap() {
        return Map.of(
                "huskit_id", id.json(),
                "huskit_container", "true"
        );
    }
}
