package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.PluginManager;

@RequiredArgsConstructor
public class AddLombokDependencies {

    private final PluginManager pluginManager;
    private final InternalProperties internalProperties;
    private final DependencyHandler dependencies;

    public void add() {
        pluginManager.withPlugin("java", plugin -> {
            var lombokDependency = internalProperties.getLib("lombok");
            dependencies.add("compileOnly", lombokDependency);
            dependencies.add("annotationProcessor", lombokDependency);
        });
    }
}
