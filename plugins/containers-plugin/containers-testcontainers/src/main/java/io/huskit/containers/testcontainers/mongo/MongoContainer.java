package io.huskit.containers.testcontainers.mongo;

import io.huskit.containers.model.MongoStartedContainer;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

@RequiredArgsConstructor
public class MongoContainer implements MongoStartedContainer {

    Log log;
    AtomicInteger counter = new AtomicInteger();
    MongoRequestedContainer mongoRequestedContainer;
    ReadWriteLock stopLock = new ReentrantReadWriteLock();
    @NonFinal
    volatile MongoDBContainer mongoDBContainer;

    @Override
    public ContainerId id() {
        return mongoRequestedContainer.id();
    }

    @Override
    public ContainerPort port() {
        return useContainer(container -> new FixedContainerPort(container.getFirstMappedPort()));
    }

    @Override
    public void start() {
        useContainer(Function.identity());
    }

    @Override
    public void close() throws Exception {
        synchronized (this) {
            try {
                stopLock.writeLock().lock();
                if (mongoDBContainer != null) {
                    if (mongoRequestedContainer.containerReuse().dontStop()) {
                        // if container is reused - drop all databases except the default ones, instead of stopping the container
                        var dropCommand = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => {if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
                        mongoDBContainer.execInContainer("/bin/sh", "-c", dropCommand);
                    } else {
                        log.info("Stopping mongo container [{}]", mongoRequestedContainer.id());
                        var before = System.currentTimeMillis();
                        mongoDBContainer.stop();
                        log.info("Stopped mongo container [{}] in [{}] ms", mongoRequestedContainer.id(), System.currentTimeMillis() - before);
                    }
                    mongoDBContainer = null;
                }
            } finally {
                stopLock.writeLock().unlock();
            }
        }
    }

    @Override
    public String connectionString() {
        var mongoContainerReuse = mongoRequestedContainer.containerReuse();
        return useContainer(container -> {
            var connectionString = container.getConnectionString();
            if (mongoContainerReuse.allowed() && mongoContainerReuse.newDatabaseForEachRequest()) {
                var dbName = mongoRequestedContainer.databaseName() + "_" + counter.incrementAndGet();
                var result = connectionString + "/" + dbName;
                log.info("Reusable connection string - " + result);
                return result;
            } else {
                log.info("Non reusable connection string - " + connectionString);
                return connectionString;
            }
        });
    }

    @Override
    public Map<String, String> environment() {
        var connectionStringEnvironmentVariableName = "MONGO_CONNECTION_STRING";
        var dbNameEnvironmentVariable = "MONGO_DB_NAME";
        var portEnvironmentVariableName = "MONGO_PORT";
        var mongoContainerReuse = mongoRequestedContainer.containerReuse();
        return useContainer(container -> {
            var connectionString = container.getConnectionString();
            if (mongoContainerReuse.allowed() && mongoContainerReuse.newDatabaseForEachRequest()) {
                var dbName = mongoRequestedContainer.databaseName() + "_" + counter.incrementAndGet();
                var result = connectionString + "/" + dbName;
                log.info("Reusable connection string - " + result);
                return Map.of(
                        connectionStringEnvironmentVariableName, result,
                        portEnvironmentVariableName, String.valueOf(port().number()),
                        dbNameEnvironmentVariable, dbName
                );
            } else {
                log.info("Non reusable connection string - " + connectionString);
                return Map.of(
                        connectionStringEnvironmentVariableName, connectionString,
                        portEnvironmentVariableName, String.valueOf(port().number()),
                        dbNameEnvironmentVariable, mongoRequestedContainer.databaseName()
                );
            }
        });
    }

    private <R> R useContainer(Function<MongoDBContainer, R> consumer) {
        MongoDBContainer container;
        try {
            stopLock.readLock().lock();
            container = mongoDBContainer;
            if (container != null) {
                log.info("Using existing mongo container [{}]", mongoRequestedContainer.id());
                return consumer.apply(container);
            }
        } finally {
            stopLock.readLock().unlock();
        }
        synchronized (this) {
            container = mongoDBContainer;
            if (container == null) {
                try {
                    stopLock.writeLock().lock();
                    TestContainersDelegate.setReuse();
                    container = new MongoDBContainer(
                            DockerImageName.parse(
                                    mongoRequestedContainer.image().value()
                            ).asCompatibleSubstituteFor("mongo")
                    );
                    if (mongoRequestedContainer.containerReuse().allowed()) {
                        container = container.withLabels(
                                Map.of(
                                        "huskit_id", id().toString(),
                                        "huskit_container", "true"
                                ));
                    } else {
                        container = container.withLabel("huskit_id", mongoRequestedContainer.source().value() + id().toString());
                    }
                    container = container.withReuse(true);
                    startAndLog(container, mongoRequestedContainer.containerReuse().newDatabaseForEachRequest());
                } finally {
                    stopLock.writeLock().unlock();
                }
            }
        }
        mongoDBContainer = container;
        return consumer.apply(container);
    }

    private void startAndLog(MongoDBContainer container, boolean isReuse) {
        var before = System.currentTimeMillis();
        container.start();
        if (isReuse) {
            log.info("Started mongo reusable container [{}] in [{}] ms", mongoRequestedContainer.id(), System.currentTimeMillis() - before);
        } else {
            log.info("Started mongo container [{}] in [{}] ms", mongoRequestedContainer.id(), System.currentTimeMillis() - before);
        }
    }
}
