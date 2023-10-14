package io.huskit.containers.model;

import io.huskit.containers.model.started.StartedContainerInternal;

import java.util.Map;

public interface MongoStartedContainer extends StartedContainerInternal {

    String connectionString();

    Map<String, String> environment();
}
