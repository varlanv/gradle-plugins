package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigureContainersPlugin implements Runnable {

    Log log;
    ProjectDescription projectDescription;
    NewOrExistingExtension extensions;
    BuildServiceRegistry sharedServices;
    TaskContainer tasks;
    Consumer<Runnable> afterEvaluateSupplier;

    @Override
    public void run() {
        var containersExtension = new PrepareContainersExtension(
            projectDescription,
            extensions
        ).get();
        afterEvaluateSupplier.accept(
            () -> {
                var containersBuildServiceProvider = new RegisterContainersBuildService(
                    log,
                    projectDescription,
                    sharedServices,
                    containersExtension
                ).register();
                new ConfigureContainers(
                    log,
                    projectDescription,
                    containersExtension,
                    tasks,
                    containersBuildServiceProvider
                ).run();
            }
        );
    }
}
