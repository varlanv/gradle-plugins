package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.GroovyPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;

@RequiredArgsConstructor
public class AddCommonPlugins {

    private final Boolean isGradlePlugin;
    private final PluginManager pluginManager;

    public void add() {
        if (isGradlePlugin) {
            pluginManager.apply(JavaGradlePluginPlugin.class);
            pluginManager.apply(GroovyPlugin.class);
            pluginManager.apply(MavenPublishPlugin.class);
        }
    }
}
