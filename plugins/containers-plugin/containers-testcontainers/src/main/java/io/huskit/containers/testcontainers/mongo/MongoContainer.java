package io.huskit.containers.testcontainers.mongo;

import io.huskit.containers.model.MongoStartedContainer;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.StartedContainerRegistry;
import io.huskit.gradle.common.function.MemoizedSupplier;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@RequiredArgsConstructor
public class MongoContainer implements MongoStartedContainer {

    public static final String CONNECTION_STRING_ENV = "MONGO_CONNECTION_STRING";
    public static final String DB_NAME_ENV = "MONGO_DB_NAME";
    public static final String PORT_ENV = "MONGO_PORT";

    static final String DEFAULT_IMAGE = "mongo:4.4.8";
    Log log;
    MongoRequestedContainer mongoRequestedContainer;
    StartedContainerRegistry startedContainerRegistry;
    AtomicInteger counter = new AtomicInteger();
    MemoizedSupplier<MongoDBContainer> mongoDBContainer = new MemoizedSupplier<>(this::getMongoDBContainer);

    @Override
    public ContainerId id() {
        return mongoRequestedContainer.id();
    }

    @Override
    public ContainerPort port() {
        return new FixedContainerPort(mongoDBContainer.get().getFirstMappedPort());
    }

    @Override
    public void start() {
        mongoDBContainer.get();
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            if (mongoDBContainer.isInitialized()) {
                if (mongoRequestedContainer.containerReuse().dontStop()) {
                    // if container is reused - drop all databases except the default ones, instead of stopping the container
                    var dropCommand = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => {if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
                    mongoDBContainer.get().execInContainer("/bin/sh", "-c", dropCommand);
                } else {
                    log.info("Stopping mongo container [{}]", mongoRequestedContainer.id().json());
                    var before = System.currentTimeMillis();
                    mongoDBContainer.get().stop();
                    log.info("Stopped mongo container [{}] in [{}] ms", mongoRequestedContainer.id().json(), System.currentTimeMillis() - before);
                }
                mongoDBContainer.reset();
            }
        }
    }

    @Override
    public String connectionString() {
        start();
        var mongoContainerReuse = mongoRequestedContainer.containerReuse();
        var connectionString = mongoDBContainer.get().getConnectionString();
        if (mongoContainerReuse.allowed() && mongoContainerReuse.newDatabaseForEachRequest()) {
            var dbName = mongoRequestedContainer.databaseName() + "_" + counter.incrementAndGet();
            var result = connectionString + "/" + dbName;
            log.info("Reusable connection string - " + result);
            return result;
        } else {
            log.info("Non reusable connection string - " + connectionString);
            return connectionString;
        }
    }

    @Override
    public Map<String, String> environment() {
        start();
        var mongoContainerReuse = mongoRequestedContainer.containerReuse();
        var connectionString = mongoDBContainer.get().getConnectionString();
        if (mongoContainerReuse.allowed() && mongoContainerReuse.newDatabaseForEachRequest()) {
            var dbName = mongoRequestedContainer.databaseName() + "_" + counter.incrementAndGet();
            var result = connectionString + "/" + dbName;
            log.info("Reusable connection string - " + result);
            return Map.of(
                    CONNECTION_STRING_ENV, result,
                    PORT_ENV, String.valueOf(port().number()),
                    DB_NAME_ENV, dbName
            );
        } else {
            log.info("Non reusable connection string - " + connectionString);
            return Map.of(
                    CONNECTION_STRING_ENV, connectionString,
                    PORT_ENV, String.valueOf(port().number()),
                    DB_NAME_ENV, mongoRequestedContainer.databaseName()
            );
        }
    }

    private Map<String, String> buildLabels() {
        return Map.of(
                "huskit_id", id().json(),
                "huskit_container", "true"
        );
    }

    private MongoDBContainer getMongoDBContainer() {
        TestContainersUtils.setReuse();
        var mongoDBContainer = new MongoDBContainer(
                DockerImageName.parse(
                        mongoRequestedContainer.image().value()
                ).asCompatibleSubstituteFor("mongo")
        ).withLabels(buildLabels()).withReuse(true);
        startAndLog(mongoDBContainer, mongoRequestedContainer.containerReuse().newDatabaseForEachRequest());
        return mongoDBContainer;
    }

    private void startAndLog(MongoDBContainer container, boolean isReuse) {
        var before = System.currentTimeMillis();
        container.start();
        if (isReuse) {
            log.info("Started mongo reusable container [{}] in [{}] ms", mongoRequestedContainer.id().json(), System.currentTimeMillis() - before);
        } else {
            log.info("Started mongo container [{}] in [{}] ms", mongoRequestedContainer.id().json(), System.currentTimeMillis() - before);
        }
    }
}
