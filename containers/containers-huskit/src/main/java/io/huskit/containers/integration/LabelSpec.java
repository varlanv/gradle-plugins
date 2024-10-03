package io.huskit.containers.integration;

import java.util.Map;

public interface LabelSpec {

    ContainerSpec pair(CharSequence key, Object value);

    ContainerSpec map(Map<String, ?> map);
}
