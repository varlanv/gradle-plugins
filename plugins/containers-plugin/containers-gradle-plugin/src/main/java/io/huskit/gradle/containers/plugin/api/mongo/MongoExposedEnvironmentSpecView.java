package io.huskit.gradle.containers.plugin.api.mongo;

public interface MongoExposedEnvironmentSpecView {

    void connectionString(String connectionString);

    void databaseName(String databaseName);

    void port(String port);
}
