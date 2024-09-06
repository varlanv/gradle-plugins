package io.huskit.containers.testcontainers.mongo;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.ContainerLabels;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.MongoStartedContainer;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.NonStartedContainer;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class MongoContainer implements MongoStartedContainer {

    Log log;
    MongoRequestedContainer request;
    AtomicInteger databaseNameCounter = new AtomicInteger();
    MemoizedSupplier<MongoDBContainer> mongoDBContainerSupplier = new MemoizedSupplier<>(this::getMongoDBContainer);
    MemoizedSupplier<ContainerPort> portSupplier = new MemoizedSupplier<>(this::_port);
    MemoizedSupplier<String> connectionStringBaseSupplier = new MemoizedSupplier<>(this::connectionStringBase);

    @Override
    public ContainerId id() {
        return request.id();
    }

    @Override
    public ContainerPort port() {
        return portSupplier.get();
    }

    @Override
    @SneakyThrows
    public NonStartedContainer stop() {
        synchronized (this) {
            if (mongoDBContainerSupplier.isInitialized()) {
                if (request.reuseOptions().enabled() && request.reuseOptions().dontStopOnClose()) {
                    // if container is reused - drop all databases except the default ones, instead of stopping the container
                    var dropCommand = "mongo --eval 'db.adminCommand(\"listDatabases\").databases.forEach(d => {if(![\"admin\", \"config\", \"local\"].includes(d.name)) { db.getSiblingDB(d.name).dropDatabase();} });'";
                    mongoDBContainerSupplier.get().execInContainer("/bin/sh", "-c", dropCommand);
                    log.info("Dropped all databases except the default ones in mongo container [{}]", request.id().json());
                } else {
                    var before = System.currentTimeMillis();
                    mongoDBContainerSupplier.get().stop();
                    log.info("Stopped mongo container in [{}] ms, key=[{}]", request.id().json(), System.currentTimeMillis() - before);
                }
                mongoDBContainerSupplier.reset();
            }
        }
        return this;
    }

    @Override
    public ContainerType type() {
        return ContainerType.MONGO;
    }

    @Override
    public MongoContainer start() {
        mongoDBContainerSupplier.get();
        return this;
    }

    @Override
    public String connectionString() {
        start();
        var mongoContainerReuse = request.reuseOptions();
        var connectionStringBase = connectionStringBaseSupplier.get();
        if (mongoContainerReuse.enabled() && mongoContainerReuse.newDatabaseForEachRequest()) {
            var dbName = request.databaseName() + "_" + databaseNameCounter.incrementAndGet();
            var result = connectionStringBase + "/" + dbName;
            log.info("Reusable connection string - " + result);
            return result;
        } else {
            log.info("Non reusable connection string - " + connectionStringBase);
            return connectionStringBase;
        }
    }

    @Override
    public Map<String, String> environment() {
        start();
        var mongoContainerReuse = request.reuseOptions();
        var connectionString = connectionStringBaseSupplier.get();
        var mongoExposedEnvironment = request.exposedEnvironment();
        if (mongoContainerReuse.enabled() && mongoContainerReuse.newDatabaseForEachRequest()) {
            var dbName = request.databaseName() + "_" + databaseNameCounter.incrementAndGet();
            var connectionStringWithDb = connectionString + "/" + dbName;
            log.info("Reusable connection string - [{}]", connectionStringWithDb);
            return Map.of(
                    mongoExposedEnvironment.connectionString(), connectionStringWithDb,
                    mongoExposedEnvironment.port(), String.valueOf(port().number()),
                    mongoExposedEnvironment.databaseName(), dbName
            );
        } else {
            log.info("Non reusable connection string - [{}]", connectionString);
            return Map.of(
                    mongoExposedEnvironment.connectionString(), connectionString,
                    mongoExposedEnvironment.port(), String.valueOf(port().number()),
                    mongoExposedEnvironment.databaseName(), request.databaseName()
            );
        }
    }

    private String connectionStringBase() {
        return mongoDBContainerSupplier.get().getConnectionString();
    }

    private ContainerPort _port() {
        return new FixedContainerPort(mongoDBContainerSupplier.get().getFirstMappedPort());
    }

    private MongoDBContainer getMongoDBContainer() {
        var mongoDBContainer = new MongoDBContainer(
                DockerImageName.parse(
                        request.image().value()
                ).asCompatibleSubstituteFor("mongo")
        ).withLabels(new ContainerLabels(id()).asMap()).withReuse(true);
        startAndLog(mongoDBContainer, request.reuseOptions().enabled());
        return mongoDBContainer;
    }

    private void startAndLog(MongoDBContainer container, boolean reuseEnabled) {
        var before = System.currentTimeMillis();
        container.start();
        var key = request.id().json();
        var time = System.currentTimeMillis() - before;
        if (reuseEnabled) {
            log.info("Started mongo reusable container in [{}] ms, key=[{}]", key, time);
        } else {
            log.info("Started mongo non-reusable container in [{}] ms, key=[{}] ", key, time);
        }
    }
}
