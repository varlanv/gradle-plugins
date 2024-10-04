package io.huskit.containers.model;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Predicate;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HtConstants {

    public static final Long ZERO_INSTANT_MILLIS = -62135596800000L;
    public static final String CONTAINER_HASH_LABEL = "HTCT_REUSABLE_HASH";
    public static final String CONTAINER_CLEANUP_AFTER_LABEL = "HTCT_CLEANUP_AFTER";
    public static final String CONTAINER_STARTED_AT_LABEL = "HTCT_STARTED_AT";
    public static final String KEY_LABEL = "huskit_key";

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Cleanup {

        public static final Duration DEFAULT_CLEANUP_AFTER = Duration.of(12, ChronoUnit.HOURS);
    }

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Predicates {

        private static final Predicate<?> ALWAYS_TRUE = t -> true;
        private static final Predicate<?> ALWAYS_FALSE = t -> false;

        @SuppressWarnings("unchecked")
        public static <T> Predicate<T> alwaysTrue() {
            return (Predicate<T>) ALWAYS_TRUE;
        }

        @SuppressWarnings("unchecked")
        public static <T> Predicate<T> alwaysFalse() {
            return (Predicate<T>) ALWAYS_FALSE;
        }
    }

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Mongo {

        public static final Integer DEFAULT_PORT = 27017;
        public static final String DROP_COMMAND = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => "
                + "{if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
        public static final String DEFAULT_CONNECTION_STRING_ENV = "MONGO_CONNECTION_STRING";
        public static final String DEFAULT_DB_NAME_ENV = "MONGO_DB_NAME";
        public static final String DEFAULT_PORT_ENV = "MONGO_PORT";
        public static final String DEFAULT_IMAGE = "mongo:4.4.8";
        public static final String DEFAULT_DB_NAME = "mongoHuskitContainerDb";
        public static final String CONNECTION_STRING_PATTERN = "mongodb://%s:%d";
    }
}
