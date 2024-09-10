package io.huskit.gradle.aspectj.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitAspectjPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle("HuskitAspectjPlugin.apply");
    }
}
