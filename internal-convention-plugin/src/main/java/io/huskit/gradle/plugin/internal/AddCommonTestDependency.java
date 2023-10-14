package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;

@RequiredArgsConstructor
public class AddCommonTestDependency {

    private final PluginManager pluginManager;
    private final String projectPath;
    private final InternalEnvironment environment;
    private final DependencyHandler dependencies;
    private final Provider<Project> commonTestProject;

    public void add() {
        if (!environment.isTest() && !projectPath.equals(":common-test")) {
            pluginManager.withPlugin("java", plugin -> {
                dependencies.add("testImplementation", commonTestProject);
            });
        }
    }
}
