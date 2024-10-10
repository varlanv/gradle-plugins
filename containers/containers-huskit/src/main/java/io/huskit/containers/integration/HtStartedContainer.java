package io.huskit.containers.integration;

import java.util.Map;

public interface HtStartedContainer {

    String id();

    Map<String, String> properties();

    String hash();

    void stopAndRemove();

    Boolean isStopped();
}
