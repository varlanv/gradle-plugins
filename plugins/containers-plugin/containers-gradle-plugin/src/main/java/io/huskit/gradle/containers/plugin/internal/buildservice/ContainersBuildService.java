package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.ContainersRequest;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.testcontainers.mongo.ActualTestContainersDelegate;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.core.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.ContainersServiceRequest;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.log.GradleLog;
import io.huskit.log.ProfileLog;
import lombok.experimental.NonFinal;
import org.gradle.api.services.BuildService;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    private static final AtomicInteger counter = new AtomicInteger();
    private static final ActualTestContainersDelegate testContainersDelegate = new ActualTestContainersDelegate(new GradleLog(ContainersBuildService.class));
    private volatile @NonFinal ContainersApplication application;
    AtomicLong timeStarted = new AtomicLong();

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    @SuppressWarnings("resource")
    public List<StartedContainer> containers(ContainersServiceRequest request) {
        var app = application;
        if (app == null) {
            synchronized (this) {
                app = application;
                if (app == null) {
                    timeStarted.set(System.currentTimeMillis());
                    var log = new GradleLog(ContainersBuildService.class);
                    application = app = ContainersApplication.application(
                            log,
                            Optional.ofNullable(request.testContainersDelegate()).orElse(testContainersDelegate)
                    );
                }
            }
        }
        return app.containers(
                new ContainersRequest(
                        request.taskLog(),
                        request.projectDescription(),
                        request.requestSpec().get().stream().map(ContainerRequestSpec::toRequestedContainer).collect(Collectors.toList())
                )
        );
    }

    @Override
    public void close() throws Exception {
        var app = application;
        if (app != null) {
            ProfileLog.withProfile("io.huskit.gradle.containers.core.ContainersApplication.close", app::close);
        }
        new GradleLog(ContainersBuildService.class).error("------------------------------------------Finished in [{}]ms key [{}]----------------------------------------",
                Duration.ofMillis(System.currentTimeMillis() - timeStarted.get()).toMillis(), counter.getAndIncrement());
    }
}
