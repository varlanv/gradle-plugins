package io.huskit.containers.model.request;

import io.huskit.common.HtConstants;
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
            this(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV, HtConstants.Mongo.DEFAULT_DB_NAME_ENV, HtConstants.Mongo.DEFAULT_PORT_ENV);
        }
    }
}
