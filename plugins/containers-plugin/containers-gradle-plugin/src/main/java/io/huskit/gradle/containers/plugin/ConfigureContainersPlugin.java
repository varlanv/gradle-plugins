package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigureContainersPlugin {

    Log log;
    ProjectDescription projectDescription;
    ObjectFactory objects;
    NewOrExistingExtension extensions;
    BuildServiceRegistry sharedServices;
    TaskContainer tasks;
    Consumer<Action<Project>> afterEvaluateSupplier;

    public void configure() {
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
                new ConfigureContainersAfterProjectEvaluate(
                        log,
                        projectDescription,
                        dockerContainersExtension,
                        tasks,
                        containersBuildServiceProvider
                )
        );
    }
}
