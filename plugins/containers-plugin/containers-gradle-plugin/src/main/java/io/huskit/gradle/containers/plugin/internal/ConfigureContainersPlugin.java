package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.log.Log;
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
        var dockerContainersExtension = new PrepareContainersExtension(
                log,
                projectDescription,
                extensions
        ).get();
        var containersBuildServiceProvider = new RegisterContainersBuildService(
                log,
                sharedServices
        ).register();
        afterEvaluateSupplier.accept(
                new ConfigureContainers(
                        log,
                        projectDescription,
                        dockerContainersExtension,
                        tasks,
                        containersBuildServiceProvider
                )
        );
    }
}
