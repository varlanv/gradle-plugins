package io.huskit.gradle.containers.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitContainersPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var objects = project.getObjects();
        var tasks = project.getTasks();
        var projectPath = project.getPath();
        var projectName = project.getName();
        var projectDescription = new GradleProjectDescription(
                projectPath,
                projectName
        );
        new ConfigureContainersPlugin(
                new GradleProjectLog(
                        HuskitContainersPlugin.class,
                        projectDescription
                ),
                projectDescription,
                objects,
                extensions,
                project.getGradle().getSharedServices(),
                tasks,
                project::afterEvaluate
        ).configure();
    }
}
