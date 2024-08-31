package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;

@RequiredArgsConstructor
public class AddTestDependencies {

    private final PluginManager pluginManager;
    private final String projectPath;
    private final InternalEnvironment environment;
    private final DependencyHandler dependencies;
    private final Provider<Project> commonTestProject;
    private final InternalProperties properties;

    public void add() {
        if (!environment.isTest() && !projectPath.equals(":common-test")) {
            pluginManager.withPlugin("java", plugin -> {
                dependencies.add("testImplementation", commonTestProject);
            });
        }
        pluginManager.withPlugin("java", plugin -> {
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("junit-jupiter-api"));
            dependencies.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, properties.getLib("junit-platform-launcher"));
        });
    }
}
