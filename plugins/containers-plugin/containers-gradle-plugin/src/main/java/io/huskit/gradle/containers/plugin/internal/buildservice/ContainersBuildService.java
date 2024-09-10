package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.common.Volatile;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.services.BuildService;

import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    private static final MultiBuildState multiBuildState = new MultiBuildState();
    SingleBuildState singleBuildState = new SingleBuildState();

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    public List<StartedContainer> containers(ContainersServiceRequest request) {
        singleBuildState.timeStarted().syncSetOrGet(System::currentTimeMillis);
        @SuppressWarnings("resource")
        var app = singleBuildState.application().syncSetOrGet(() -> {
            var log = new GradleLog(ContainersBuildService.class);
            return ContainersApplication.application(
                    log,
                    Optional.ofNullable(request.testContainersDelegate()).orElse(multiBuildState.testContainersDelegate())
            );
        });
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
        singleBuildState.application().maybe().ifPresent(app -> {
            ProfileLog.withProfile("io.huskit.gradle.containers.core.ContainersApplication.close", app::close);
            new GradleLog(ContainersBuildService.class)
                    .error("------------------------------------------Finished in [{}]ms key [{}]----------------------------------------",
                            Duration.ofMillis(System.currentTimeMillis() - singleBuildState.timeStarted().require()),
                            multiBuildState.counter().getAndIncrement());
        });
    }

    @Getter
    @RequiredArgsConstructor
    static class SingleBuildState implements Serializable {

        Volatile<Long> timeStarted = Volatile.of();
        Volatile<ContainersApplication> application = Volatile.of();
    }

    @Getter
    @RequiredArgsConstructor
    static class MultiBuildState implements Serializable {

        AtomicInteger counter = new AtomicInteger();
        ActualTestContainersDelegate testContainersDelegate = new ActualTestContainersDelegate(
                new GradleLog(
                        ContainersBuildService.class
                )
        );
    }
}
