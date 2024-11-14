package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.spec.AbstractShouldStartBeforeSpec;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Optional;

@RequiredArgsConstructor
public class ConfigureContainers implements Runnable {

    Log log;
    ProjectDescription projectDescription;
    HuskitContainersExtension dockerContainersExtension;
    TaskContainer tasks;
    Provider<ContainersBuildService> containersBuildServiceProvider;

    @Override
    public void run() {
        Optional.ofNullable(dockerContainersExtension.getShouldStartBeforeSpec().getOrNull())
            .ifPresentOrElse(this::handleShouldStartBeforeSpec, this::handleShouldStartBeforeSpecNotPresent);
    }

    private void handleShouldStartBeforeSpec(AbstractShouldStartBeforeSpec shouldStartBeforeSpec) {
        getShouldRunBeforeTaskProvider(shouldStartBeforeSpec)
            .or(() -> getTaskTaskProviderFromTaskName(shouldStartBeforeSpec))
            .ifPresentOrElse(dependentTaskProvider -> {
                var containersTaskProvider = new RegisterContainersTask(
                    log,
                    projectDescription,
                    tasks,
                    dockerContainersExtension,
                    containersBuildServiceProvider,
                    dependentTaskProvider.getName()
                ).register();
                dependentTaskProvider.configure(configureContainerDependentTaskAction(containersTaskProvider));
            }, () -> {
                var dependentTask = shouldStartBeforeSpec.getShouldRunBeforeTask().get();
                var containersTask = new RegisterContainersTask(
                    log,
                    projectDescription,
                    tasks,
                    dockerContainersExtension,
                    containersBuildServiceProvider,
                    dependentTask.getName()
                ).register();
                var configureContainerDependentTask = configureContainerDependentTaskAction(containersTask);
                configureContainerDependentTask.configure(dependentTask);
            });
    }

    private void handleShouldStartBeforeSpecNotPresent() {
        log.info(() -> "No containers will be started, because no task was specified to run before containers start");
    }

    private Optional<TaskProvider<Task>> getTaskTaskProviderFromTaskName(AbstractShouldStartBeforeSpec shouldStartBeforeSpec) {
        return shouldStartBeforeSpec.getShouldRunBeforeTaskName().map(val -> {
            log.info(() -> "Using task name to find task provider for shouldRunBeforeTask: [%s]".formatted(val));
            return Optional.of(tasks.named(val));
        }).getOrElse(Optional.empty());
    }

    private Optional<TaskProvider<Task>> getShouldRunBeforeTaskProvider(AbstractShouldStartBeforeSpec shouldStartBeforeSpec) {
        return shouldStartBeforeSpec.getShouldRunBeforeTaskProvider().map(val -> {
            log.info(() -> "Found task provider for shouldRunBeforeTask: [%s]".formatted(val.getName()));
            return Optional.of(val);
        }).getOrElse(Optional.empty());
    }

    private ConfigureContainerDependentTask configureContainerDependentTaskAction(TaskProvider<ContainersTask> containersTask) {
        return new ConfigureContainerDependentTask(
            log,
            projectDescription,
            containersTask,
            containersBuildServiceProvider,
            dockerContainersExtension
        );
    }
}
