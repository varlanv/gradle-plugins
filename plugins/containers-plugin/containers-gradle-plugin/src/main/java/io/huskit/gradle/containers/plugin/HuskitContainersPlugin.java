package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.gradle.containers.plugin.internal.ConfigureContainersPlugin;
import io.huskit.log.GradleProjectLog;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitContainersPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var projectPath = project.getPath();
        var projectName = project.getName();
        var projectDescription = new ProjectDescription.Default(
                project.getRootProject().getName(),
                projectPath,
                projectName
        );
        var log = new GradleProjectLog(
                HuskitContainersPlugin.class,
                projectPath,
                projectName
        );

        new ConfigureContainersPlugin(
                log,
                projectDescription,
                new NewOrExistingExtension(
                        log,
                        project.getExtensions()
                ),
                project.getGradle().getSharedServices(),
                project.getTasks(),
                runnable -> project.afterEvaluate(ignore -> runnable.run())
        ).run();
    }
}
