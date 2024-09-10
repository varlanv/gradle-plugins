package io.huskit.gradle.containers.core;

import io.huskit.common.concurrent.ParallelFnRunner;
import io.huskit.containers.model.*;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.NonStartedContainer;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.testcontainers.mongo.MongoContainer;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.log.Log;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainersApplication implements AutoCloseable {

    Log log;
    StartedContainersRegistry startedContainersRegistry;

    public static ContainersApplication application(Log commonLog, TestContainersDelegate testContainersDelegate) {
        testContainersDelegate.setReuse();
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

    public List<StartedContainer> containers(ContainersRequest request) {
        return new ValidatedDockerContainers(
                new DockerContainers(
                        log,
                        startedContainersRegistry,
                        request.requestedContainers()
                ),
                request.requestedContainers()
        ).start();
    }

    @Override
    public void close() throws Exception {
        new ParallelFnRunner<StartedContainer, NonStartedContainer>(
                startedContainersRegistry.all()
                        .map(container -> (Supplier<StartedContainer>) () -> (StartedContainer) container)
                        .collect(Collectors.toList()))
                .doParallel(this::tryClose);
    }

    private void tryClose(StartedContainer container) {
        try {
            container.stop();
        } catch (Exception e) {
            // TODO add verification for log
            log.error("Failed to stop container [{}]. Ignoring exception - [{}]", container.id(), e.getMessage());
        }
    }
}
