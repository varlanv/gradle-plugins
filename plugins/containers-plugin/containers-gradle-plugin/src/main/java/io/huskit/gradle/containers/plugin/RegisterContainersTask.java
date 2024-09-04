package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.string.CapitalizedString;
import io.huskit.gradle.containers.plugin.internal.ContainersTask;
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

@RequiredArgsConstructor
public class RegisterContainersTask {

    Log log;
    ProjectDescription projectDescription;
    TaskContainer tasks;
    DockerContainersExtension dockerContainersExtension;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    String dependentTaskName;

    public TaskProvider<ContainersTask> register() {
        var taskName = ContainersTask.nameForTask(dependentTaskName);
        log.info("Registering containers task with name: [{}]", taskName);
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
