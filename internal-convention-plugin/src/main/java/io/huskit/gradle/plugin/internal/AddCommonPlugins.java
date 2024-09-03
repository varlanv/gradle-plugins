package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;

@RequiredArgsConstructor
public class AddCommonPlugins {

    Boolean isGradlePlugin;
    PluginManager pluginManager;

    public void add() {
        if (isGradlePlugin) {
            pluginManager.apply(JavaGradlePluginPlugin.class);
            pluginManager.apply(JavaPlugin.class);
            pluginManager.apply(MavenPublishPlugin.class);
        }
    }
}
