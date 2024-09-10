package io.huskit.gradle.containers.plugin.internal.spec.mongo;

import io.huskit.gradle.containers.plugin.api.mongo.MongoExposedEnvironmentSpecView;
import org.gradle.api.provider.Property;

public interface MongoExposedEnvironmentSpec extends MongoExposedEnvironmentSpecView {

    Property<String> getConnectionString();

    Property<String> getDatabaseName();

    Property<String> getPort();

    @Override
    default void connectionString(String connectionString) {
        getConnectionString().set(connectionString);
    }

    @Override
    default void databaseName(String databaseName) {
        getDatabaseName().set(databaseName);
    }

    @Override
    default void port(String port) {
        getPort().set(port);
    }
}
