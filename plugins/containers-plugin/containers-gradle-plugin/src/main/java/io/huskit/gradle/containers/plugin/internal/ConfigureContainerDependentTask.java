package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

@RequiredArgsConstructor
public class ConfigureContainerDependentTask implements Action<Task> {

    Log log;
    ProjectDescription projectDescription;
    TaskProvider<ContainersTask> containersTaskProvider;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    HuskitContainersExtension dockerContainersExtension;

    public void configure(Task dependentTask) {
        log.info(() -> "Adding [%s] task as dependency to task: [%s]".formatted(containersTaskProvider.getName(), dependentTask.getName()));
        dependentTask.dependsOn(containersTaskProvider);
        dependentTask.mustRunAfter(containersTaskProvider);
        dependentTask.usesService(containersBuildServiceProvider);
        new MaybeAddContainersEnvironment(
            log,
            projectDescription,
            dependentTask,
            containersBuildServiceProvider,
            dockerContainersExtension
        ).maybeAdd();
    }

    @Override
    public void execute(Task task) {
        configure(task);
    }
}
