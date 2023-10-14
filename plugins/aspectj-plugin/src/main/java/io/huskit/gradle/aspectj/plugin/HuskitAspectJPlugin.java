package io.huskit.gradle.aspectj.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitAspectJPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle("HuskitAspectJPlugin.apply");
    }
}
