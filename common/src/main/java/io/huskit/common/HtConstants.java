package io.huskit.common;

import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HtConstants {

    public static final String TEST_SYSTEM_PROPERTY = "htct.test";
    public static final Long ZERO_INSTANT_MILLIS = -62135596800000L;
    public static final String CONTAINER_HASH_LABEL = "HTCT_REUSABLE_HASH";
    public static final String CONTAINER_CLEANUP_AFTER_LABEL = "HTCT_CLEANUP_AFTER";
    public static final String CONTAINER_STARTED_AT_LABEL = "HTCT_STARTED_AT";
    public static final String CONTAINER_LABEL = "HTCT_CONTAINER";
    public static final String CONTAINER_PROJECT_KEY = "HTCT_PROJECT_KEY";
    public static final String GRADLE_ROOT_PROJECT = "HTCT_GRADLE_ROOT_PROJECT";
    public static final String NPIPE_SOCKET = "\\\\.\\pipe\\docker_engine";
    public static final String UNIX_SOCKET = "/var/run/docker.sock";

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Cleanup {

        public static final Duration DEFAULT_CLEANUP_AFTER = Duration.of(18, ChronoUnit.HOURS);
    }

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Consumers {

        private static final Consumer<?> NO_OP = t -> {
        };

        @SuppressWarnings("unchecked")
        public static <T> Consumer<T> noop() {
            return (Consumer<T>) NO_OP;
        }
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
        public static final String NEW_DB_EACH_REQUEST = "MONGO_NEW_DB_EACH_REQUEST";
        public static final String DEFAULT_IMAGE = "mongo:4.4.8";
        public static final String DEFAULT_DB_NAME = "mongoHuskitContainerDb";
        public static final BiFunction<Object, Object, String> CONNECTION_STRING_PATTERN = (host, port) -> "mongodb://" + host + ":" + port;
    }
}
