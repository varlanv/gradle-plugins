package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;

@RequiredArgsConstructor
public class AddTestDependencies {

    PluginManager pluginManager;
    String projectPath;
    InternalEnvironment environment;
    DependencyHandler dependencies;
    Provider<Project> commonTestProject;
    InternalProperties properties;

    public void add() {
        if (!environment.isTest() && !projectPath.equals(":common-test")) {
            pluginManager.withPlugin("java", plugin -> {
                dependencies.add("testImplementation", commonTestProject);
            });
        }
        if (projectPath.equals(":common-test")) {
            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
        }
        pluginManager.withPlugin("java", plugin -> {
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("junit-jupiter-api"));
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
            dependencies.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, properties.getLib("junit-platform-launcher"));
        });
    }
}
