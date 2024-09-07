package io.huskit.gradle.containers.plugin.api.mongo;

import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import org.gradle.api.Action;

public interface MongoContainerRequestSpecView extends ContainerRequestSpecView {

    void databaseName(String databaseName);

    void reuse(Action<MongoContainerReuseSpecView> action);

    void exposedEnvironment(Action<MongoExposedEnvironmentSpecView> action);
}
