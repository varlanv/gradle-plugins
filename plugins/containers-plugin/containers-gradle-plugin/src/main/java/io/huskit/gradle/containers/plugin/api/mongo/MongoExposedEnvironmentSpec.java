package io.huskit.gradle.containers.plugin.api.mongo;

import org.gradle.api.provider.Property;

public interface MongoExposedEnvironmentSpec {

    Property<String> getConnectionString();

    Property<String> getDatabaseName();

    Property<String> getPort();
}
