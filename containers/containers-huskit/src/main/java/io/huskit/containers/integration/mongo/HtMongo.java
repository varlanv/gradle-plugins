package io.huskit.containers.integration.mongo;

import io.huskit.common.Mutable;
import io.huskit.common.port.DynamicContainerPort;
import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.api.docker.HtDocker;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.integration.*;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.HtConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class HtMongo implements HtServiceContainer {

    HtImgName imageName;
    Mutable<DefContainerSpec> containerSpec;
    Mutable<Boolean> newDatabaseForEachRequest;
    Mutable<String> databaseName;
    DefDockerClientSpec dockerClientSpec;
    AtomicInteger databaseNameCounter = new AtomicInteger(0);

    private HtMongo(HtImgName imageName) {
        this.imageName = imageName;
        this.containerSpec = Mutable.of(
                new DefContainerSpec(imageName, containerType())
                        .addProperty(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV, HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV)
                        .addProperty(HtConstants.Mongo.DEFAULT_DB_NAME_ENV, HtConstants.Mongo.DEFAULT_DB_NAME_ENV)
                        .addProperty(HtConstants.Mongo.DEFAULT_PORT_ENV, HtConstants.Mongo.DEFAULT_PORT_ENV)
        );
        this.dockerClientSpec = new DefDockerClientSpec(this);
        this.containerSpec.require().await().forLogMessageContaining("Waiting for connections");
        this.newDatabaseForEachRequest = Mutable.of(false);
        this.databaseName = Mutable.of(HtConstants.Mongo.DEFAULT_DB_NAME);
    }

    public static HtMongo fromImage(CharSequence image) {
        return new HtMongo(HtImgName.of(image));
    }

    @Override
    public HtMongo withDockerClientSpec(Consumer<DockerClientSpec> dockerClientSpecAction) {
        dockerClientSpecAction.accept(dockerClientSpec);
        return this;
    }

    @Override
    public HtMongo withContainerSpec(Consumer<ContainerSpec> containerSpecAction) {
        containerSpecAction.accept(containerSpec.require());
        return this;
    }

    public HtMongo withContainerSpec(DefContainerSpec defContainerSpec) {
        containerSpec.set(defContainerSpec);
        return this;
    }

    public HtMongo withNewDatabaseForEachRequest(Boolean newDatabaseForEachRequest) {
        this.newDatabaseForEachRequest.set(newDatabaseForEachRequest);
        return this;
    }

    @Override
    public MongoStartedContainer start() {
        var reuseEnabled = containerSpec.require().reuseSpec().value().check(ReuseWithTimeout::enabled);
        var htDocker = dockerClientSpec.docker().or(() -> {
            var docker = HtDocker.anyClient();
            if (!reuseEnabled) {
                // todo instead of disabling cleanup on close, add it to specific containers
                return docker.withCleanOnClose(true);
            }
            return docker;
        });
        var container = findExisting(htDocker.containers(), reuseEnabled).orElseGet(() -> {
            containerSpec.require().labels().pair(HtConstants.CONTAINER_STARTED_AT_LABEL, System.currentTimeMillis());
            var containers = htDocker.containers();
            var port = containerSpec.require().ports().port().mapOr(p -> Map.entry(p.hostValue(), p.containerValue().orElseThrow()),
                    () -> Map.entry(new DynamicContainerPort().hostValue(), HtConstants.Mongo.DEFAULT_PORT));
            return containers
                    .run(imageName.reference(), runSpec -> {
                                var envSpec = containerSpec.require().envSpec();
                                envSpec.envMap().ifPresent(runSpec::withEnv);
                                var labelSpec = containerSpec.require().labelSpec();
                                labelSpec.labelMap().ifPresent(runSpec::withLabels);
                                var waitSpec = containerSpec.require().waitSpec();
                                waitSpec.textWait().ifPresent(waiter -> runSpec.withLookFor(waiter.text(), waiter.duration()));
                                runSpec.withPortBinding(port.getKey(), port.getValue());
                            }
                    )
                    .exec();
        });
        var port = container.network().firstMappedPort();
        var dbName = Mutable.<String>of();
        Supplier<Map<String, String>> mapSupplier = () -> {
            var connectionString = Mutable.<String>of();
            if (reuseEnabled && newDatabaseForEachRequest.require()) {
                var counter = databaseNameCounter.incrementAndGet();
                var db = databaseName.require() + "_" + counter;
                var conn = String.format(HtConstants.Mongo.CONNECTION_STRING_PATTERN,
                        "localhost", container.network().firstMappedPort()) + "/" + db;
                dbName.set(db);
                connectionString.set(conn);
            } else {
                var conn = String.format(HtConstants.Mongo.CONNECTION_STRING_PATTERN,
                        "localhost", container.network().firstMappedPort());
                dbName.set(databaseName.require());
                connectionString.set(conn);
            }
            return Map.of(
                    containerSpec.require().properties().get(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV), connectionString.require(),
                    containerSpec.require().properties().get(HtConstants.Mongo.DEFAULT_DB_NAME_ENV), dbName.require(),
                    containerSpec.require().properties().get(HtConstants.Mongo.DEFAULT_PORT_ENV), String.valueOf(port)
            );
        };
        return new DfMongoStartedContainer(
                container.id(),
                reuseEnabled,
                newDatabaseForEachRequest.require(),
                () -> mapSupplier.get().get(containerSpec.require().properties().get(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV)),
                () -> containerSpec.require().hash(),
                mapSupplier,
                htDocker
        );
    }

    @Override
    public ContainerType containerType() {
        return ContainerType.MONGO;
    }

    private Optional<HtContainer> findExisting(HtContainers htContainers, Boolean reuseEnabled) {
        if (reuseEnabled) {
            var before = System.currentTimeMillis();
            var reuseWithTimeout = containerSpec.require().reuseSpec().value().require();
            var hash = containerSpec.require().hash();
            System.out.println("Looking for container with hash: " + hash);
            var containers = htContainers.list(listSpec -> listSpec.withLabelFilter(HtConstants.CONTAINER_HASH_LABEL, hash))
                    .asList();
            if (containers.size() == 1) {
                System.out.printf("Found container with hash: '%s', lookup took %s%n",
                        hash, Duration.ofMillis(System.currentTimeMillis() - before));
                var container = containers.get(0);
                var now = Instant.now();
                var cleanupAfterTime = container.createdAt().plus(reuseWithTimeout.cleanupAfter());
                if (now.isAfter(cleanupAfterTime)) {
                    htContainers.remove(container.id(), spec -> spec.withForce().withVolumes()).exec();
                } else {
                    return Optional.of(container);
                }
            } else if (containers.size() > 1) {
                System.out.println("Find more than one container with hash: " + hash);
                htContainers.remove(
                        containers.stream().map(HtContainer::id).collect(Collectors.toList()),
                        spec -> spec.withForce().withVolumes()
                );
            } else {
                containerSpec.require().labels()
                        .map(Map.of(
                                HtConstants.CONTAINER_HASH_LABEL, hash,
                                HtConstants.CONTAINER_CLEANUP_AFTER_LABEL, reuseWithTimeout.cleanupAfter().toSeconds()
                        ));
            }
            System.out.printf("Didn't find container with hash: '%s', lookup took %s%n",
                    hash, Duration.ofMillis(System.currentTimeMillis() - before));
        }
        return Optional.empty();
    }

    public interface MongoStartedContainer extends HtStartedContainer {

        String connectionString();
    }

    @Getter
    @RequiredArgsConstructor
    private static class DfMongoStartedContainer implements MongoStartedContainer {

        String id;
        Boolean reuseEnabled;
        Boolean newDatabaseForEachRequest;
        Supplier<String> conStr;
        Supplier<String> hashSupplier;
        Supplier<Map<String, String>> props;
        HtDocker htDocker;
        AtomicBoolean stopped = new AtomicBoolean(false);

        @Override
        public Map<String, String> properties() {
            return Collections.unmodifiableMap(props.get());
        }

        @Override
        public String hash() {
            return hashSupplier.get();
        }

        @Override
        public void stopAndRemove() {
            synchronized (this) {
                if (reuseEnabled && newDatabaseForEachRequest) {
                    htDocker.containers().execInContainer(id, "/bin/sh", List.of("-c", HtConstants.Mongo.DROP_COMMAND));
                } else {
                    if (stopped.compareAndSet(false, true)) {
                        htDocker.containers().remove(id, s -> s.withVolumes().withForce()).exec();
                    }
                }
            }
        }

//        @Override
//        @SneakyThrows
//        public NonStartedContainer stop() {
//            synchronized (this) {
//                if (mongoDbContainerSupplier.isInitialized()) {
//                    if (request.reuseOptions().enabled() && request.reuseOptions().reuseBetweenBuilds()) {
//                        // if container is reused - drop all databases except the default ones, instead of stopping the container
//                        testContainersDelegate.execInContainer(mongoDbContainerSupplier, "/bin/sh", "-c", HtConstants.Mongo.DROP_COMMAND);
//                        log.info("Dropped all databases except the default ones in mongo container [{}]", request.key().json());
//                    } else {
//                        var before = System.currentTimeMillis();
//                        testContainersDelegate.stop(mongoDbContainerSupplier);
//                        log.info("Stopped mongo container in [{}] ms, key=[{}]", request.key().json(), System.currentTimeMillis() - before);
//                    }
//                    mongoDbContainerSupplier.reset();
//                }
//            }
//            return this;
//        }

        @Override
        public Boolean isStopped() {
            return stopped.get();
        }

        @SuppressWarnings("all")
        public static void main(String[] args) {
            var fullTime = System.currentTimeMillis();
            var mongo = HtMongo.fromImage(HtConstants.Mongo.DEFAULT_IMAGE)
                    .withContainerSpec(containerSpec -> containerSpec
                            .reuse().enabledWithCleanupAfter(Duration.ofSeconds(60))
                            .env().pair("KEaA", "vaka")
                    )
                    .start();
            var connectionString = mongo.connectionString();
//        MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.8").withReuse(true);
//        mongoDBContainer.start();
//        var connectionString = mongoDBContainer.getReplicaSetUrl();
//        verifyMongoConnection(connectionString);
            System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - fullTime));
        }

        @Override
        public String connectionString() {
            return conStr.get();
        }
    }
}
