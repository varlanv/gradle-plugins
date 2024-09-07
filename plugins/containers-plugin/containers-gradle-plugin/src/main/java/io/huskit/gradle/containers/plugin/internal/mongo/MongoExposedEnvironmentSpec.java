package io.huskit.gradle.containers.plugin.internal.mongo;

import io.huskit.gradle.containers.plugin.api.mongo.MongoExposedEnvironmentSpecView;
import org.gradle.api.provider.Property;

public interface MongoExposedEnvironmentSpec extends MongoExposedEnvironmentSpecView {

    Property<String> getConnectionString();

    Property<String> getDatabaseName();

    Property<String> getPort();

    default void connectionString(String connectionString) {
        getConnectionString().set(connectionString);
    }

    default void databaseName(String databaseName) {
        getDatabaseName().set(databaseName);
    }

    default void port(String port) {
        getPort().set(port);
    }
}
