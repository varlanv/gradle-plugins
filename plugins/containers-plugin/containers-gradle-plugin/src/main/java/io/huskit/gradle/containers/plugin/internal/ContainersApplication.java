package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.*;
import io.huskit.containers.model.request.ContainersRequest;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.ContainerLauncher;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainerInternal;
import io.huskit.containers.model.started.StartedContainersInternal;
import io.huskit.containers.testcontainers.mongo.MongoContainer;
import io.huskit.containers.testcontainers.mongo.TestContainersUtils;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.GradleLog;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ContainersApplication implements AutoCloseable {

    Log log;
    StartedContainersInternal startedContainersInternal;

    public Containers containers(ContainersRequest request) {
        return new ValidatedDockerContainers(
                new DockerContainers(
                        log,
                        startedContainersInternal,
                        request.requestedContainers()
                ),
                request.requestedContainers()
        );
    }

    private void tryStop(StartedContainer startedContainer) {
        try {
            startedContainer.close();
        } catch (Exception exception) {
            log.error("Failed to stop container [{}]. Ignoring exception", startedContainer.id().json(), exception);
        }
    }

    public static ContainersApplication application() {
        var commonLog = new GradleLog(ContainersBuildService.class);
        var testContainersUtils = new TestContainersUtils();
        return new ContainersApplication(
                commonLog,
                new DockerStartedContainersInternal(
                        commonLog,
                        new KnownDockerContainers(
                                commonLog,
                                Map.of(
                                        ContainerType.MONGO, requestedContainer -> new MongoContainer(
                                                commonLog,
                                                (MongoRequestedContainer) requestedContainer,
                                                testContainersUtils
                                        )
                                )
                        )
                )
        );
    }

    @Override
    public void close() throws Exception {
        var startedContainers = startedContainersInternal.list();
        new ContainerLauncher(
                log,
                startedContainers.stream()
                        .map(container -> (Supplier<StartedContainerInternal>) () -> (StartedContainerInternal) container)
                        .collect(Collectors.toList()))
                .stop();
        if (!startedContainers.isEmpty()) {
            var timeMillis = System.currentTimeMillis();
            if (startedContainers.size() == 1) {
                tryStop(startedContainers.get(0));
                log.lifecycle("Stopped single container in [{}] ms", System.currentTimeMillis() - timeMillis);
            } else {
                var countDownLatch = new CountDownLatch(startedContainers.size());
                var executorService = Executors.newFixedThreadPool(startedContainers.size());
                for (var startedContainer : startedContainers) {
                    executorService.execute(() -> {
                        try {
                            tryStop(startedContainer);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
                countDownLatch.await(10, TimeUnit.SECONDS);
                log.lifecycle("Stopped [{}] containers in [{}] ms", startedContainers.size(), System.currentTimeMillis() - timeMillis);
            }
        }
    }
}
