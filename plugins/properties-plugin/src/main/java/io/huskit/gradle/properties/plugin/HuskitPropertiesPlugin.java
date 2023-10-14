package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.DefaultProps;
import io.huskit.gradle.common.plugin.model.props.Props;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionContainer;

public class HuskitPropertiesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ExtensionContainer extensions = project.getExtensions();
        Logger logger = project.getLogger();
        if (extensions.findByName(Props.EXTENSION_NAME) == null) {
            extensions.add(
                    Props.class,
                    Props.EXTENSION_NAME,
                    new DefaultProps(
                            project.getProviders(),
                            extensions.getExtraProperties()
                    )
            );
            logger.info("Added extension: {}", Props.EXTENSION_NAME);
        } else {
            logger.info("Extension already exists: {}. Skipping...", Props.EXTENSION_NAME);
        }
    }
}
