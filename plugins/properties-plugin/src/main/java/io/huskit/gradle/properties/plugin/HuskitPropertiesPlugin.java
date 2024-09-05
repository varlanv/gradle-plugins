package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.DefaultProps;
import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.log.GradleLog;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HuskitPropertiesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var log = new GradleLog(HuskitPropertiesPlugin.class);
        if (extensions.findByName(Props.EXTENSION_NAME) == null) {
            extensions.add(
                    Props.class,
                    Props.EXTENSION_NAME,
                    new DefaultProps(
                            project.getProviders(),
                            extensions.getExtraProperties()
                    )
            );
            log.info("Added extension: [{}]", Props.EXTENSION_NAME);
        } else {
            log.info("Extension already exists: [{}]. Skipping...", Props.EXTENSION_NAME);
        }
    }
}
