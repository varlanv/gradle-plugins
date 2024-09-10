package io.huskit.containers.model;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Constants {

    public static final String KEY_LABEL = "huskit_key";

    @RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Cleanup {

        public static final Duration DEFAULT_CLEANUP_AFTER = Duration.of(12, ChronoUnit.HOURS);
    }

    @RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Mongo {

        public static final Integer DEFAULT_PORT = 27017;
        public static final String DROP_COMMAND = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => "
                + "{if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
        public static final String DEFAULT_CONNECTION_STRING_ENV = "MONGO_CONNECTION_STRING";
        public static final String DEFAULT_DB_NAME_ENV = "MONGO_DB_NAME";
        public static final String DEFAULT_PORT_ENV = "MONGO_PORT";
        public static final String DEFAULT_IMAGE = "mongo:4.4.8";
        public static final String DEFAULT_DB_NAME = "mongoHuskitContainerDb";
    }
}
