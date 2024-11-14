package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.common.ProfileLog;
import io.huskit.common.Volatile;
import io.huskit.containers.core.ContainersApplication;
import io.huskit.containers.integration.HtIntegratedDocker;
import io.huskit.containers.integration.HtStartedContainer;
import io.huskit.containers.model.ContainersRequest;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.ContainersServiceRequest;
import io.huskit.gradle.containers.plugin.internal.GradleLog;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.services.BuildService;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    private static final SharedBuildState sharedBuildState = new SharedBuildState();
    SingleBuildState singleBuildState = new SingleBuildState();

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    public Map<String, HtStartedContainer> containers(ContainersServiceRequest request) {
        singleBuildState.timeStarted().syncSetOrGet(System::currentTimeMillis);
        @SuppressWarnings("resource")
        var app = singleBuildState.application().syncSetOrGet(() -> {
            var log = new GradleLog(ContainersBuildService.class);
            return ContainersApplication.application(
                log,
                Objects.requireNonNullElseGet(request.integratedDocker(), sharedBuildState::integratedDocker)
            );
        });
        return app.containers(
            new ContainersRequest(
                request.taskLog(),
                request.projectDescription(),
                request.requestSpec().get().stream()
                    .map(ContainerRequestSpec::toContainerSpec)
                    .collect(Collectors.toList())
            )
        );
    }

    @Override
    public void close() throws Exception {
        singleBuildState.application().ifPresent(
            app -> {
                ProfileLog.withProfile(
                    "io.huskit.gradle.containers.core.ContainersApplication.close",
                    app.log(),
                    app::close
                );
                new GradleLog(ContainersBuildService.class)
                    .error(
                        () -> "------------------------------------------Finished in [%s]ms key [%s]----------------------------------------"
                            .formatted(
                                Duration.ofMillis(System.currentTimeMillis() - singleBuildState.timeStarted().require()),
                                sharedBuildState.counter().getAndIncrement())
                    );
            }
        );
    }

    @Getter
    @RequiredArgsConstructor
    static class SingleBuildState implements Serializable {

        Volatile<Long> timeStarted = Volatile.of();
        Volatile<ContainersApplication> application = Volatile.of();
    }

    @Getter
    @RequiredArgsConstructor
    static class SharedBuildState implements Serializable {

        AtomicInteger counter = new AtomicInteger();
        HtIntegratedDocker integratedDocker = HtIntegratedDocker.instance();
    }
}
