package io.huskit.gradle.containers.plugin;

import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrepareContainersExtension {

    Log log;
    ProjectDescription projectDescription;
    NewOrExistingExtension newOrExistingExtension;

    public DockerContainersExtension prepare() {
        log.info("Adding containers extension: [{}]", ContainersExtension.name());
        var extension = newOrExistingExtension.getOrCreate(
                ContainersExtension.class,
                DockerContainersExtension.class,
                ContainersExtension.name()
        );
        extension.getRootProjectName().set(projectDescription.rootProjectName());
        extension.getProjectName().set(projectDescription.name());
        extension.getProjectPath().set(projectDescription.path());
        return extension;
    }
}
