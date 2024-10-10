package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.integration.FakeHtIntegratedDocker;
import io.huskit.containers.integration.HtIntegratedDocker;
import io.huskit.containers.integration.HtStartedContainer;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.string.CapitalizedString;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.log.GradleProjectLog;
import io.huskit.log.ProfileLog;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Map;
import java.util.stream.Collectors;

@DisableCachingByDefault(because = "Caching of containers is not supported")
public abstract class ContainersTask extends DefaultTask {

    public static String name() {
        return "startContainers";
    }

    public static String nameForTask(String taskName) {
        return name() + "For" + CapitalizedString.capitalize(taskName);
    }

    public static String doFirstActionName() {
        return "startContainers";
    }

    @Internal
    public abstract Property<ContainersBuildService> getContainersBuildService();

    @Internal
    public abstract Property<ProjectDescription> getProjectDescription();

    @Input
    public abstract ListProperty<ContainerRequestSpec> getRequestedContainers();


    @TaskAction
    public void startContainers() {
        startAndReturnContainers(HtIntegratedDocker.instance());
    }

    @VisibleForTesting
    public Map<String, HtStartedContainer> startAndReturnContainers() {
        return startAndReturnContainers(new FakeHtIntegratedDocker());
    }

    @VisibleForTesting
    public Map<String, HtStartedContainer> startAndReturnContainers(HtIntegratedDocker integratedDocker) {
        var projectDescription = getProjectDescription().get();
        var log = new GradleProjectLog(
                ContainersTask.class,
                projectDescription.path(),
                projectDescription.name()
        );
        return ProfileLog.withProfile(
                "io.huskit.gradle.containers.plugin.internal.ContainersTask.getStartedContainers",
                log,
                () -> getStartedContainers(integratedDocker, log, projectDescription));
    }

    private Map<String, HtStartedContainer> getStartedContainers(HtIntegratedDocker integratedDocker,
                                                                 GradleProjectLog log,
                                                                 ProjectDescription projectDescription) {
        var containerRequestSpecs = getRequestedContainers().get();
        if (containerRequestSpecs.isEmpty()) {
            return Map.of();
        }
        var startedContainers = getContainersBuildService().get().containers(
                new ContainersServiceRequest(
                        log,
                        projectDescription,
                        getRequestedContainers(),
                        integratedDocker
                )
        );
        if (startedContainers.isEmpty()) {
            log.info("No containers were started");
        } else {
            log.info("Started [{}] containers: [{}]", startedContainers.size(), startedContainers.values().stream()
                    .map(HtStartedContainer::id)
                    .collect(Collectors.toList())
            );
        }
        return startedContainers;
    }
}
