package io.huskit.containers.integration.mongo;

import io.huskit.common.Volatile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DefEnvSpec implements EnvSpec {

    ContainerSpec parent;
    @Getter
    Volatile<Map<String, String>> envMap = Volatile.of();

    @Override
    public ContainerSpec pair(CharSequence key, Object value) {
        envMap.set(
                Map.of(
                        key.toString(),
                        value.toString()
                )
        );
        return parent;
    }

    @Override
    public ContainerSpec map(Map<String, Object> map) {
        var env = map.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().toString()
                        )
                );
        envMap.set(Collections.unmodifiableMap(env));
        return parent;
    }
}
