package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.PluginManager;

@RequiredArgsConstructor
public class AddSpockDependencies {

    private final PluginManager pluginManager;
    private final InternalProperties internalProperties;
    private final DependencyHandler dependencies;

    public void add() {
        pluginManager.withPlugin("groovy", plugin -> {
            String spockVersion = internalProperties.get("spockVersion").map(version -> "org.spockframework:spock-core:" + version).get();
            String groovyVersion = internalProperties.get("groovyVersion").map(version -> "org.codehaus.groovy:groovy-all:" + version).get();
            dependencies.add("testImplementation", spockVersion);
            dependencies.add("testImplementation", groovyVersion);
        });
    }
}
