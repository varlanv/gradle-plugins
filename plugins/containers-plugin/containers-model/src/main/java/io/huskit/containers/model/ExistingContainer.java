package io.huskit.containers.model;

import java.util.Map;

public interface ExistingContainer {

    String huskitId();

    String containerId();

    Long createdAt();

    Map<String, String> labels();

}
