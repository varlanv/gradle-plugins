package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Map;
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
        if (shouldStartBeforeSpec.isSet()) {
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
        } else {
            log.info("No containers will be started, because shouldStartBefore is set to false");
        }
    }

    private void handleShouldStartBeforeSpecNotPresent() {
        log.info("No containers will be started, because no task was specified to run before containers start");
    }

    private Optional<TaskProvider<Task>> getTaskTaskProviderFromTaskName(AbstractShouldStartBeforeSpec shouldStartBeforeSpec) {
        var taskName = shouldStartBeforeSpec.getShouldRunBeforeTaskName().getOrNull();
        if (taskName != null) {
            log.info("Using task name to find task provider for shouldRunBeforeTask: [{}]", taskName);
            return Optional.of(tasks.named(taskName));
        } else {
            log.info("No task name found for shouldRunBeforeTask");
            return Optional.empty();
        }
    }

    private Optional<TaskProvider<Task>> getShouldRunBeforeTaskProvider(AbstractShouldStartBeforeSpec shouldStartBeforeSpec) {
        var provider = shouldStartBeforeSpec.getShouldRunBeforeTaskProvider().getOrNull();
        if (provider != null) {
            log.info("Found task provider for shouldRunBeforeTask: [{}]", provider.getName());
            return Optional.of(provider);
        } else {
            log.info("No task provider found for shouldRunBeforeTask");
            return Optional.empty();
        }
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
