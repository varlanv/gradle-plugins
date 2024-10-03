package io.huskit.containers.api;

import java.time.Instant;
import java.util.Map;

public interface HtContainer {

    String id();

    String name();

    Map<String, String> labels();

    Instant createdAt();

    Integer firstMappedPort();
}
