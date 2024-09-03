package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;

@RequiredArgsConstructor
public class AddLombokDependencies {

    PluginManager pluginManager;
    InternalProperties internalProperties;
    DependencyHandler dependencies;

    public void add() {
        pluginManager.withPlugin("java", plugin -> {
            var lombokDependency = internalProperties.getLib("lombok");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
        });
    }
}
