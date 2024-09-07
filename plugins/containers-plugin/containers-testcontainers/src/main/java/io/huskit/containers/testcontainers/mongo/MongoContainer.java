package io.huskit.containers.testcontainers.mongo;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.*;
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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class MongoContainer implements MongoStartedContainer {

    Log log;
    TestContainersDelegate testContainersDelegate;
    MongoRequestedContainer request;
    AtomicInteger databaseNameCounter = new AtomicInteger();
    MemoizedSupplier<MongoDBContainer> mongoDBContainerSupplier = new MemoizedSupplier<>(this::getMongoDBContainer);
    MemoizedSupplier<ContainerPort> portSupplier = new MemoizedSupplier<>(this::_port);
    MemoizedSupplier<String> connectionStringBaseSupplier = new MemoizedSupplier<>(this::connectionStringBase);
    MemoizedSupplier<Optional<ExistingContainer>> existingContainerSupplier = new MemoizedSupplier<>(this::existingContainer);

    AtomicBoolean isStarted = new AtomicBoolean();

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
                if (request.reuseOptions().enabled() && request.reuseOptions().reuseBetweenBuilds()) {
                    // if container is reused - drop all databases except the default ones, instead of stopping the container
                    testContainersDelegate.execInContainer(mongoDBContainerSupplier, "/bin/sh", "-c", Constants.Mongo.DROP_COMMAND);
                    log.info("Dropped all databases except the default ones in mongo container [{}]", request.id().json());
                } else {
                    var before = System.currentTimeMillis();
                    testContainersDelegate.stop(mongoDBContainerSupplier);
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
        if (isStarted.compareAndSet(false, true)) {
            var cleanupAfter = request.reuseOptions().cleanup().cleanupAfter();
            if (!cleanupAfter.isZero()) {
                existingContainerSupplier.get().ifPresent(existingContainer -> {
                    if (existingContainer.isExpired(cleanupAfter)) {
                        log.info("Existing mongo container is expired (started at [{}], current time is [{}], timeout is [{}]), stopping it - [{}] ",
                                Instant.ofEpochMilli(existingContainer.createdAt()),
                                Instant.now(),
                                cleanupAfter,
                                request.id().json());
                        testContainersDelegate.remove(existingContainer);
                    }
                });
            }
            testContainersDelegate.start(mongoDBContainerSupplier.get());
        }
        return this;
    }

    @Override
    public Map<String, String> environment() {
        start();
        var mongoContainerReuse = request.reuseOptions();
        var connectionString = connectionStringBaseSupplier.get();
        var mongoExposedEnvironment = request.exposedEnvironment();
        var port = String.valueOf(port().number());
        if (mongoContainerReuse.enabled() && mongoContainerReuse.newDatabaseForEachRequest()) {
            var counter = databaseNameCounter.incrementAndGet();
            var dbName = request.databaseName() + "_" + counter;
            return Map.of(
                    mongoExposedEnvironment.connectionString(), connectionString + "/" + dbName,
                    mongoExposedEnvironment.port(), port,
                    mongoExposedEnvironment.databaseName(), dbName
            );
        } else {
            return Map.of(
                    mongoExposedEnvironment.connectionString(), connectionString,
                    mongoExposedEnvironment.port(), port,
                    mongoExposedEnvironment.databaseName(), request.databaseName()
            );
        }
    }

    private Optional<ExistingContainer> existingContainer() {
        return testContainersDelegate.getExistingContainer(request.id());
    }

    private String connectionStringBase() {
        return testContainersDelegate.getConnectionString(mongoDBContainerSupplier);
    }

    private ContainerPort _port() {
        return new FixedContainerPort(testContainersDelegate.getFirstMappedPort(mongoDBContainerSupplier));
    }

    private MongoDBContainer getMongoDBContainer() {
        return new MongoDBContainer(
                DockerImageName.parse(request.image().value()).asCompatibleSubstituteFor("mongo"))
                .withLabels(new ContainerLabels(id()).asMap())
                .withReuse(true);
    }
}
