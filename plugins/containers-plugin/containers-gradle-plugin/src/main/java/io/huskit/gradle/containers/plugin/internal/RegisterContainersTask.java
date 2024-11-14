package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

@RequiredArgsConstructor
public class RegisterContainersTask {

    Log log;
    ProjectDescription projectDescription;
    TaskContainer tasks;
    HuskitContainersExtension dockerContainersExtension;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    String dependentTaskName;

    public TaskProvider<ContainersTask> register() {
        var taskName = ContainersTask.nameForTask(dependentTaskName);
        log.info(() -> "Registering containers task with name: [%s]".formatted(taskName));
        return tasks.register(
            taskName,
            ContainersTask.class,
            containersTask -> {
                containersTask.getProjectDescription().set(projectDescription);
                containersTask.getRequestedContainers().addAll(dockerContainersExtension.getContainersRequestedByUser().get());
                containersTask.getContainersBuildService().set(containersBuildServiceProvider);
                containersTask.usesService(containersBuildServiceProvider);
            });
    }
}
