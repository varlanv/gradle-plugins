package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.MongoStartedContainer;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.log.Log;
import io.huskit.log.ProfileLog;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;
import org.jetbrains.annotations.Nullable;

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

    public Map<String, String> executeAndReturn(Task task, @Nullable TestContainersDelegate testContainersDelegate) {
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
                                        testContainersDelegate
                                )
                        );
                        if (!startedContainers.isEmpty()) {
                            var startedContainer = (MongoStartedContainer) startedContainers.stream().findFirst().get();
                            log.info("Adding containers environment to task: [{}]", task.getName());
                            var environment = startedContainer.environment();
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
