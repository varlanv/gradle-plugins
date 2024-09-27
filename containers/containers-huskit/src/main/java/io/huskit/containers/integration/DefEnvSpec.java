package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefEnvSpec implements EnvSpec {

    ContainerSpec parent;
    @Getter
    Mutable<Map<String, String>> envMap = Volatile.of(new HashMap<>());

    @Override
    public ContainerSpec pair(CharSequence key, Object value) {
        envMap.require().put(key.toString(), value.toString());
        return parent;
    }

    @Override
    public ContainerSpec map(Map<String, Object> map) {
        var envMap = this.envMap.require();
        for (var entry : map.entrySet()) {
            envMap.put(entry.getKey(), entry.getValue().toString());
        }
        return parent;
    }
}
