package io.huskit.containers.internal;

import io.huskit.common.collection.HtCollections;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.HtContainerNetwork;
import io.huskit.containers.api.JsonHtContainerConfig;
import io.huskit.containers.api.JsonHtContainerNetwork;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
public class HtJsonContainer implements HtContainer {

    Map<String, Object> source;

    @Override
    public String id() {
        return HtCollections.getFromMap("Id", source);
    }

    @Override
    public String name() {
        return HtCollections.getFromMap("Name", source);
    }

    @Override
    public JsonHtContainerConfig config() {
        return new JsonHtContainerConfig(HtCollections.getFromMap("Config", source));
    }

    @Override
    public HtContainerNetwork network() {
        return new JsonHtContainerNetwork(HtCollections.getFromMap("NetworkSettings", source));
    }

    @Override
    public Instant createdAt() {
        return Instant.parse(Objects.requireNonNull((String) source.get("Created"), "CreatedAt info is not present"));
    }
}
