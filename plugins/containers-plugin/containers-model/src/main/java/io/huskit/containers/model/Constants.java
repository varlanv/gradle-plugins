package io.huskit.containers.model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Constants {

    private Constants() {
    }

    public static class Cleanup {

        public static final Duration DEFAULT_CLEANUP_AFTER = Duration.of(12, ChronoUnit.HOURS);
    }

    public static class Mongo {

        public static final Integer DEFAULT_PORT = 27017;
        public static final String DROP_COMMAND = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => {if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
        public static final String DEFAULT_CONNECTION_STRING_ENV = "MONGO_CONNECTION_STRING";
        public static final String DEFAULT_DB_NAME_ENV = "MONGO_DB_NAME";
        public static final String DEFAULT_PORT_ENV = "MONGO_PORT";
        public static final String DEFAULT_IMAGE = "mongo:4.4.8";
        public static final String DEFAULT_DB_NAME = "mongoHuskitContainerDb";
    }
}
