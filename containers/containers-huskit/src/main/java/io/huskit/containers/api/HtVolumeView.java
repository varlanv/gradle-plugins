package io.huskit.containers.api;

import java.time.Instant;
import java.util.Map;

public interface HtVolumeView {

    String id();

    Instant createdAt();

    String driver();

    Map<String, String> labels();

    String mountPoint();

    Map<String, String> options();

    String scope();
}
