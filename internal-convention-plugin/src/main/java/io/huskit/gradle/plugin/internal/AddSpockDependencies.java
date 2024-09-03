package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.PluginManager;

@RequiredArgsConstructor
public class AddSpockDependencies {

    PluginManager pluginManager;
    InternalProperties internalProperties;
    DependencyHandler dependencies;

    // spock was removed from the project for now
    public void add() {
//        pluginManager.withPlugin("groovy", plugin -> {
//            var spockVersion = internalProperties.getLib("spock-core");
//            var groovyVersion = internalProperties.getLib("groovy-all");
//            dependencies.add("testImplementation", spockVersion);
//            dependencies.add("testImplementation", groovyVersion);
//        });
    }
}
