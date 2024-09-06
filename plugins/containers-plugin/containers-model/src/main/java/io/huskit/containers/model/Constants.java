package io.huskit.containers.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static class Mongo {

        public static final String DEFAULT_CONNECTION_STRING_ENV = "MONGO_CONNECTION_STRING";
        public static final String DEFAULT_DB_NAME_ENV = "MONGO_DB_NAME";
        public static final String DEFAULT_PORT_ENV = "MONGO_PORT";
        public static final String DEFAULT_IMAGE = "mongo:4.4.8";
    }
}
