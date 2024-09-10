package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;

@RequiredArgsConstructor
public class AddCommonDependencies {

    PluginManager pluginManager;
    InternalProperties internalProperties;
    DependencyHandler dependencies;
    InternalEnvironment environment;
    InternalProperties properties;
    String projectPath;
    Provider<Project> commonTestProject;

    public void add() {
        pluginManager.withPlugin("java", plugin -> {
            var lombokDependency = internalProperties.getLib("lombok");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
            var jetbrainsAnnotations = internalProperties.getLib("jetbrains-annotations");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
            dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
            if (!environment.isTest() && !projectPath.equals(":common-test")) {
                dependencies.add("testImplementation", commonTestProject);
            }
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("junit-jupiter-api"));
//            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
            dependencies.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, properties.getLib("junit-platform-launcher"));

            var checkerFramework = internalProperties.getLib("checkerframework-qual");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, checkerFramework);
        });

        if (projectPath.equals(":common-test")) {
            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
//            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
        }
    }
}
