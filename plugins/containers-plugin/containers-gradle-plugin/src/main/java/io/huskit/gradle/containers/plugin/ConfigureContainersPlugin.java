package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.Log;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ConfigureContainersPlugin {

    private final Log log;
    private final ProjectDescription projectDescription;
    private final ObjectFactory objects;
    private final ExtensionContainer extensions;
    private final BuildServiceRegistry sharedServices;
    private final TaskContainer tasks;
    private final Consumer<Action<Project>> afterEvaluateSupplier;

    public void configure() {
        var newOrExistingExtension = new NewOrExistingExtension(extensions);
        var dockerContainersExtension = new PrepareContainersExtension(
                log,
                projectDescription,
                objects,
                newOrExistingExtension
        ).prepare();
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
