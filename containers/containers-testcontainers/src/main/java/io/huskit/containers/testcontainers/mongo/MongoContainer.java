package io.huskit.containers.testcontainers.mongo;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.port.ContainerPort;
import io.huskit.common.port.ResolvedPort;
import io.huskit.containers.model.*;
import io.huskit.containers.model.id.ContainerKey;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.NonStartedContainer;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public final class MongoContainer implements MongoStartedContainer {

    Log log;
    TestContainersDelegate testContainersDelegate;
    MongoRequestedContainer request;
    AtomicInteger databaseNameCounter = new AtomicInteger();
    MemoizedSupplier<MongoDBContainer> mongoDbContainerSupplier =  MemoizedSupplier.of(this::getMongoDbContainer);
    MemoizedSupplier<ContainerPort> portSupplier = MemoizedSupplier.of(this::resolvePort);
    MemoizedSupplier<String> connectionStringBaseSupplier = MemoizedSupplier.of(this::connectionStringBase);
    MemoizedSupplier<Optional<ExistingContainer>> existingContainerSupplier = MemoizedSupplier.of(this::existingContainer);

    AtomicBoolean isStarted = new AtomicBoolean();

    @Override
    public ContainerKey key() {
        return request.key();
    }

    @Override
    public ContainerPort port() {
        return portSupplier.get();
    }

    @Override
    @SneakyThrows
    public NonStartedContainer stop() {
        synchronized (this) {
            if (mongoDbContainerSupplier.isInitialized()) {
                if (request.reuseOptions().enabled() && request.reuseOptions().reuseBetweenBuilds()) {
                    // if container is reused - drop all databases except the default ones, instead of stopping the container
                    testContainersDelegate.execInContainer(mongoDbContainerSupplier, "/bin/sh", "-c", HtConstants.Mongo.DROP_COMMAND);
                    log.info("Dropped all databases except the default ones in mongo container [{}]", request.key().json());
                } else {
                    var before = System.currentTimeMillis();
                    testContainersDelegate.stop(mongoDbContainerSupplier);
                    log.info("Stopped mongo container in [{}] ms, key=[{}]", request.key().json(), System.currentTimeMillis() - before);
                }
                mongoDbContainerSupplier.reset();
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
                        log.info("Existing mongo container is expired (started at [{}], current time is [{}], "
                                        + "timeout is [{}]), stopping it - [{}] ",
                                Instant.ofEpochMilli(existingContainer.createdAt()),
                                Instant.now(),
                                cleanupAfter,
                                request.key().json());
                        testContainersDelegate.remove(existingContainer);
                    }
                });
            }
            testContainersDelegate.start(mongoDbContainerSupplier.get());
        }
        return this;
    }

    @Override
    public Map<String, String> environment() {
        start();
        var mongoContainerReuse = request.reuseOptions();
        var connectionString = connectionStringBaseSupplier.get();
        var mongoExposedEnvironment = request.exposedEnvironment();
        var port = String.valueOf(port().hostValue());
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
        return testContainersDelegate.getExistingContainer(request.key());
    }

    private String connectionStringBase() {
        return testContainersDelegate.getConnectionString(mongoDbContainerSupplier);
    }

    private ContainerPort resolvePort() {
        var requestPort = request.port();
        var firstMappedPort = testContainersDelegate.getFirstMappedPort(mongoDbContainerSupplier);
        if (requestPort.isFixed()) {
            return new ResolvedPort(firstMappedPort, requestPort.containerValue().orElseThrow(), true);
        } else {
            return new ResolvedPort(firstMappedPort, 1/*todo not 1*/, true);
        }
    }

    private MongoDBContainer getMongoDbContainer() {
        var port = request.port();
        var hostPort = port.hostValue();
        var mongoDbContainer = new MongoDBContainer(
                DockerImageName.parse(request.image().id()).asCompatibleSubstituteFor("mongo"))
                .withLabels(new ContainerLabels(key()).asMap())
                .withReuse(true);
        if (port.isFixed()) {
            return mongoDbContainer.withCreateContainerCmdModifier(cmd ->
                    Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                            new PortBinding(
                                    Ports.Binding.bindPort(hostPort),
                                    new ExposedPort(port.containerValue().orElseThrow()))));
        }
        return mongoDbContainer;
    }
}
