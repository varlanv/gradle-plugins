package io.huskit.gradle.spock.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitSpockPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle("HuskitSpockPlugin.apply");
    }
}
