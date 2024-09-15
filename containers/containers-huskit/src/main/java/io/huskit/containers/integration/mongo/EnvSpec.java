package io.huskit.containers.integration.mongo;

import java.util.Map;

public interface EnvSpec {

    ContainerSpec pair(CharSequence key, Object value);

    ContainerSpec map(Map<String, Object> map);
}
