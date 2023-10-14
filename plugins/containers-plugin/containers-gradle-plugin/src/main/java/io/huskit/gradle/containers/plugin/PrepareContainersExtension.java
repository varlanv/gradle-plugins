package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.Log;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.model.ObjectFactory;

@RequiredArgsConstructor
public class PrepareContainersExtension {

    private final Log log;
    private final ProjectDescription projectDescription;
    private final ObjectFactory objects;
    private final NewOrExistingExtension newOrExistingExtension;

    public DockerContainersExtension prepare() {
        log.info("Adding containers extension: [{}]", ContainersExtension.name());
        DockerContainersExtension extension = (DockerContainersExtension) newOrExistingExtension.getOrCreate(
                ContainersExtension.class,
                ContainersExtension.name(),
                () -> objects.newInstance(DockerContainersExtension.class)
        );
        extension.getProjectName().set(projectDescription.name());
        extension.getProjectPath().set(projectDescription.path());
        return extension;
    }
}
