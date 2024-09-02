package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.Containers;
import io.huskit.containers.model.DockerContainers;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainersInternal;
import io.huskit.containers.testcontainers.mongo.MongoContainer;
import io.huskit.gradle.common.function.MemoizedSupplier;
import io.huskit.gradle.common.plugin.model.BuildEndAware;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersRequest;
import io.huskit.log.GradleLog;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ContainersApplication implements BuildEndAware {

    Log log;
    Supplier<StartedContainersInternal> startedContainersInternal;

    public Containers containers(ContainersRequest request) {
        return new ValidatedDockerContainers(
                new DockerContainers(
                        log,
                        startedContainersInternal.get(),
                        request.requestedContainers()
                ),
                request.requestedContainers()
        );
    }

    private void tryStop(StartedContainer startedContainer) {
        try {
            startedContainer.close();
        } catch (Exception exception) {
            log.error("Failed to stop container [{}]. Ignoring exception", startedContainer.id(), exception);
        }
    }

    public static ContainersApplication application() {
        var commonLog = new GradleLog(ContainersBuildService.class);
        commonLog.info("containersApplication is not created, entered synchronized block to create instance");
        return new ContainersApplication(
                commonLog,
                new MemoizedSupplier<>(
                        () -> new DockerStartedContainersInternal(
                                commonLog,
                                new KnownDockerContainers(
                                        commonLog,
                                        Map.of(
                                                ContainerType.MONGO, requestedContainer -> new MongoContainer(
                                                        commonLog,
                                                        (MongoRequestedContainer) requestedContainer
                                                )
                                        )
                                )
                        )
                ));
    }

    @Override
    public void onBuildEnd() throws Exception {
        var startedContainers = startedContainersInternal.get().list();
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
                countDownLatch.await(5, TimeUnit.SECONDS);
                log.lifecycle("Stopped [{}] containers in [{}] ms", startedContainers.size(), System.currentTimeMillis() - timeMillis);
            }
        }
    }
}
