package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class PrepareContainersExtension implements Supplier<HuskitContainersExtension> {

    Log log;
    ProjectDescription projectDescription;
    NewOrExistingExtension newOrExistingExtension;

    @Override
    public HuskitContainersExtension get() {
        log.info("Adding containers extension: [{}]", ContainersExtension.name());
        var extension = newOrExistingExtension.getOrCreate(
                ContainersExtension.class,
                HuskitContainersExtension.class,
                ContainersExtension.name()
        );
        extension.getRootProjectName().set(projectDescription.rootProjectName());
        extension.getProjectName().set(projectDescription.name());
        extension.getProjectPath().set(projectDescription.path());
        return extension;
    }
}
