package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class DefLabelSpec implements LabelSpec {

    ContainerSpec parent;
    @Getter
    Mutable<Map<String, String>> labelMap = Mutable.of(new HashMap<>());

    @Override
    public ContainerSpec pair(CharSequence key, Object value) {
        labelMap.require().put(key.toString(), value.toString());
        return parent;
    }

    @Override
    public ContainerSpec map(Map<String, ?> map) {
        var labelMap = this.labelMap.require();
        for (var entry : map.entrySet()) {
            labelMap.put(entry.getKey(), entry.getValue().toString());
        }
        return parent;
    }
}
