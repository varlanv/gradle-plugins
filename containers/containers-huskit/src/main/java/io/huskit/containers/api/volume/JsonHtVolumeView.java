package io.huskit.containers.api.volume;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class JsonHtVolumeView implements HtVolumeView {

    @Getter
    String id;
    Map<String, Object> jsonMap;

    @Override
    public Instant createdAt() {
        return Instant.parse(Objects.requireNonNull((String) jsonMap.get("CreatedAt"), "CreatedAt info is not present"));
    }

    @Override
    public String driver() {
        return Objects.requireNonNull((String) jsonMap.get("Driver"), "Driver info is not present");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> labels() {
        return Optional.ofNullable(jsonMap.get("Labels"))
                .map(labels -> Collections.unmodifiableMap((Map<String, String>) labels))
                .orElseThrow(() -> new IllegalStateException("Labels info is not present"));
    }

    @Override
    public String mountPoint() {
        return Objects.requireNonNull((String) jsonMap.get("Mountpoint"), "Mountpoint info is not present");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> options() {
        return Optional.ofNullable(jsonMap.get("Options"))
                .map(options -> Collections.unmodifiableMap((Map<String, String>) options))
                .orElseThrow(() -> new IllegalStateException("Options info is not present"));
    }

    @Override
    public String scope() {
        return Objects.requireNonNull((String) jsonMap.get("Scope"), "Scope info is not present");
    }
}
