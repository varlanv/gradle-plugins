package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.DefaultProps;
import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.log.GradleLog;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Optional;

public class HuskitPropertiesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var log = new GradleLog(HuskitPropertiesPlugin.class);
        Optional.ofNullable(extensions.findByName(Props.name()))
                .ifPresentOrElse(
                        ext -> log.info("Extension already exists: [{}]. Skipping...", Props.name()),
                        () -> {
                            extensions.add(
                                    Props.class,
                                    Props.name(),
                                    new DefaultProps(
                                            project.getProviders(),
                                            extensions.getExtraProperties()
                                    )
                            );
                            log.info("Added extension: [{}]", Props.name());
                        }
                );
    }
}
