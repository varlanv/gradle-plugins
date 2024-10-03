package io.huskit.containers.internal;

import io.huskit.containers.api.HtContainer;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public class HtJsonContainer implements HtContainer {

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

    @Override
    public Instant createdAt() {
        return Instant.parse(Objects.requireNonNull((String) source.get("Created"), "CreatedAt info is not present"));
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Integer firstMappedPort() {
        Map<String, Object> networkSettings = getFromMap("NetworkSettings", source);
        Map<String, Object> ports = getFromMap("Ports", networkSettings);
        var entries = ports.entrySet();
        var mappedPorts = entries.iterator().next();
        var values = (List<Map<String, String>>) mappedPorts.getValue();
        var mappedPort = values.get(0);
        return Integer.parseInt(mappedPort.get("HostPort"));
    }

    @SuppressWarnings({"unchecked"})
    private <T> T getFromMap(String key, Map<String, Object> map) {
        return (T) Optional.ofNullable(map.get(key))
                .orElseThrow(() ->
                        new IllegalStateException(String.format("Could not find key [%s] in container map %s", key, map)));
    }
}
