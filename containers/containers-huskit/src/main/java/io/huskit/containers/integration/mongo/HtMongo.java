package io.huskit.containers.integration.mongo;

import io.huskit.common.port.DynamicContainerPort;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.HtContainers;
import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.HtImgName;
import io.huskit.containers.integration.*;
import io.huskit.containers.model.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HtMongo implements HtServiceContainer {

    HtImgName imageName;
    DefContainerSpec containerSpec;
    DefDockerClientSpec dockerClientSpec;
    ContainerHash containerHash;

    public HtMongo(HtImgName imageName) {
        this.imageName = imageName;
        this.containerSpec = new DefContainerSpec();
        this.dockerClientSpec = new DefDockerClientSpec(this);
        this.containerSpec.await().forLogMessageContaining("Waiting for connections");
        this.containerHash = new ContainerHash()
                .add(imageName.reference())
                .add(containerSpec.envSpec().envMap())
                .add(containerSpec.labelSpec().labelMap())
                .add(containerSpec.waitSpec().textWait())
                .add(containerSpec.reuseSpec().value());
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
        containerSpecAction.accept(containerSpec);
        return this;
    }

    @Override
    public MongoStartedContainer start() {
        var reuseEnabled = containerSpec.reuseSpec().value().check(ReuseWithTimeout::enabled);
        var htDocker = dockerClientSpec.docker().or(() -> {
            var docker = HtDocker.anyClient();
            if (!reuseEnabled) {
                // todo instead of disabling cleanup on close, add it to specific containers
                return docker.withCleanOnClose(true);
            }
            return docker;
        });
        var container = findExisting(htDocker.containers(), reuseEnabled).orElseGet(() -> {
            containerSpec.labels().pair(Constants.CONTAINER_STARTED_AT_LABEL, System.currentTimeMillis());
            var containers = htDocker.containers();
            var hostPort = new DynamicContainerPort().hostValue();
            return containers
                    .run(imageName.reference(), runSpec -> {
                                var envSpec = containerSpec.envSpec();
                                envSpec.envMap().ifPresent(runSpec::withEnv);
                                var labelSpec = containerSpec.labelSpec();
                                labelSpec.labelMap().ifPresent(runSpec::withLabels);
                                var waitSpec = containerSpec.waitSpec();
                                waitSpec.textWait().ifPresent(waiter -> runSpec.withLookFor(waiter.text(), waiter.duration()));
                                runSpec.withPortBinding(hostPort, Constants.Mongo.DEFAULT_PORT);
                            }
                    ).exec();
        });
        return new DfMongoStartedContainer(
                String.format(Constants.Mongo.CONNECTION_STRING_PATTERN, "localhost", container.network().firstMappedPort())
        );
    }

    private Optional<HtContainer> findExisting(HtContainers htContainers, Boolean reuseEnabled) {
        if (!reuseEnabled) {
            return Optional.empty();
        } else {
            var reuseWithTimeout = containerSpec.reuseSpec().value().require();
            var hash = containerHash.compute();
            var containers = htContainers.list(listSpec -> listSpec.withLabelFilter(Constants.CONTAINER_HASH_LABEL, hash))
                    .asList();
            if (containers.size() == 1) {
                var container = containers.get(0);
                var now = Instant.now();
                var cleanupAfterTime = container.createdAt().plus(reuseWithTimeout.cleanupAfter());
                if (now.isAfter(cleanupAfterTime)) {
                    htContainers.remove(container.id(), spec -> spec.withForce().withVolumes()).exec();
                } else {
                    return Optional.of(container);
                }
            } else if (containers.size() > 1) {
                htContainers.remove(
                        containers.stream().map(HtContainer::id).collect(Collectors.toList()),
                        spec -> spec.withForce().withVolumes()
                );
            }
            containerSpec.labels()
                    .map(Map.of(
                            Constants.CONTAINER_HASH_LABEL, hash,
                            Constants.CONTAINER_CLEANUP_AFTER_LABEL, reuseWithTimeout.cleanupAfter().toSeconds()
                    ));
            return Optional.empty();
        }
    }

    public interface MongoStartedContainer extends HtStartedContainer {

        String connectionString();
    }

    @Getter
    @RequiredArgsConstructor
    private static class DfMongoStartedContainer implements MongoStartedContainer {

        String connectionString;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        var fullTime = System.currentTimeMillis();
        var mongo = HtMongo.fromImage(Constants.Mongo.DEFAULT_IMAGE)
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
}
