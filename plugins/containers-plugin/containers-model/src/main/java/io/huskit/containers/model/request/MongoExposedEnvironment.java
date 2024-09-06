package io.huskit.containers.model.request;

import io.huskit.containers.model.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface MongoExposedEnvironment {

    String connectionString();

    String databaseName();

    String port();

    @Getter
    @RequiredArgsConstructor
    class Default implements MongoExposedEnvironment {

        String connectionString;
        String databaseName;
        String port;

        public Default() {
            this(Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_PORT_ENV);
        }
    }
}
