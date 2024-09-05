package io.huskit.containers.model;

import io.huskit.containers.model.started.StartedContainer;

import java.util.Map;

public interface MongoStartedContainer extends StartedContainer {

    String connectionString();

    Map<String, String> environment();
}
