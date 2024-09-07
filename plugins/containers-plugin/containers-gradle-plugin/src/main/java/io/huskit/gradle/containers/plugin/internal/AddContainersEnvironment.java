package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ContainersRequest;
import io.huskit.containers.model.MongoStartedContainer;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

import java.util.List;

@RequiredArgsConstructor
public class AddContainersEnvironment implements Action<Task> {

    Log log;
    ProjectDescription projectDescription;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    ListProperty<ContainerRequestSpec> containersRequestedByUser;

    @Override
    public void execute(Task task) {
        executeAndReturn(task);
    }

    public List<StartedContainer> executeAndReturn(Task task) {
        if (task instanceof Test) {
            var test = (Test) task;
            var containersBuildService = containersBuildServiceProvider.get();
            var startedContainers = containersBuildService.containers(
                    new ContainersRequest(
                            log,
                            projectDescription,
                            new RequestedContainersFromGradleUser(
                                    containersRequestedByUser.get()
                            )
                    )
            ).list();
            if (!startedContainers.isEmpty()) {
                var startedContainer = (MongoStartedContainer) startedContainers.stream().findFirst().get();
                log.info("Adding containers environment to task: [{}]", task.getName());
                var environment = startedContainer.environment();
                test.setEnvironment(environment);
                return startedContainers;
            }
        } else {
            log.info("Task [{}] is not a test task, so environment variables will not be added", task.getName());
        }
        return List.of();
    }
}
