package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistry;

import java.util.Objects;

@RequiredArgsConstructor
public class RegisterContainersBuildService {

    Log log;
    ProjectDescription projectDescription;
    BuildServiceRegistry sharedServices;
    HuskitContainersExtension containersExtension;

    public Provider<ContainersBuildService> register() {
        var containersServiceName = ContainersBuildService.name();
        var buildServiceProvider = sharedServices.registerIfAbsent(
                containersServiceName,
                ContainersBuildService.class,
                spec -> {
                    log.info("Registered build service: [{}] from project [{}]", containersServiceName, projectDescription.name());
                });
        var buildServiceRegistration = Objects.requireNonNull(
                sharedServices.getRegistrations().findByName(containersServiceName)
        );
        var maxParallelUsages = buildServiceRegistration.getMaxParallelUsages();
        var maxParallelUsagesVal = maxParallelUsages.getOrNull();
        maxParallelUsages.set(maxParallelUsagesVal == null ? 1 : maxParallelUsagesVal + 1);
        return buildServiceProvider;
    }
}
