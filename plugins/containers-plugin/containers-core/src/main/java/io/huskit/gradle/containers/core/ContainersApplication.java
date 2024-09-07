package io.huskit.gradle.containers.core;

import io.huskit.common.concurrent.ParallelRunner;
import io.huskit.containers.model.*;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.NonStartedContainer;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.containers.testcontainers.mongo.MongoContainer;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.containers.testcontainers.mongo.TestContainersUtils;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContainersApplication implements AutoCloseable {

    Log log;
    StartedContainersRegistry startedContainersRegistry;

    public StartedContainers containers(ContainersRequest request) {
        return new ValidatedDockerContainers(
                new DockerContainers(
                        log,
                        startedContainersRegistry,
                        request.requestedContainers()
                ),
                request.requestedContainers()
        ).start();
    }

    public static ContainersApplication application(Log commonLog, TestContainersDelegate testContainersDelegate) {
        var testContainersUtils = new TestContainersUtils(commonLog);
        testContainersUtils.setReuse();
        return new ContainersApplication(
                commonLog,
                new StartedContainersRegistry(
                        commonLog,
                        new KnownDockerContainers(
                                commonLog,
                                Map.of(
                                        ContainerType.MONGO, requestedContainer -> new MongoContainer(
                                                commonLog,
                                                testContainersDelegate,
                                                (MongoRequestedContainer) requestedContainer
                                        )
                                )
                        )
                )
        );
    }

    @Override
    public void close() throws Exception {
        new ParallelRunner<StartedContainer, NonStartedContainer>(
                startedContainersRegistry.all()
                        .map(container -> (Supplier<StartedContainer>) () -> (StartedContainer) container)
                        .collect(Collectors.toList()))
                .doParallel(this::tryClose);
    }

    private NonStartedContainer tryClose(StartedContainer container) {
        try {
            return container.stop();
        } catch (Exception e) {
            log.error("Failed to stop container [{}]. Ignoring exception", container.id(), e);
            return null;
        }
    }
}
