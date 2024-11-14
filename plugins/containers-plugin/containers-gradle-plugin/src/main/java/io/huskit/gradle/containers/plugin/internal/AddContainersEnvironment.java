package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.common.ProfileLog;
import io.huskit.containers.integration.HtIntegratedDocker;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.Map;

@RequiredArgsConstructor
public class AddContainersEnvironment implements Action<Task> {

    Log log;
    ProjectDescription projectDescription;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    ListProperty<ContainerRequestSpec> containersRequestedByUser;

    @Override
    public void execute(Task task) {
        executeAndReturn(task, null);
    }

    public Map<String, String> executeAndReturn(Task task, HtIntegratedDocker integratedDocker) {
        if (task instanceof Test) {
            return ProfileLog.withProfile(
                "io.huskit.gradle.containers.plugin.internal.AddContainersEnvironment.executeAndReturn",
                () -> {
                    var test = (Test) task;
                    var containersBuildService = containersBuildServiceProvider.get();
                    var startedContainers = containersBuildService.containers(
                        new ContainersServiceRequest(
                            log,
                            projectDescription,
                            containersRequestedByUser,
                            integratedDocker

                        )
                    );
                    if (!startedContainers.isEmpty()) {
                        var startedContainer = startedContainers.values().stream().findFirst().get();
                        log.info(() -> "Adding containers environment to task: [%s]".formatted(task.getName()));
                        var environment = startedContainer.properties();
                        test.setEnvironment(environment);
                        return environment;
                    }
                    return Map.of();
                }
            );
        }
        return Map.of();
    }
}
