package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

@RequiredArgsConstructor
public class MaybeAddContainersEnvironment {

    Log log;
    ProjectDescription projectDescription;
    Task dependentTask;
    Provider<ContainersBuildService> containersBuildServiceProvider;
    HuskitContainersExtension dockerContainersExtension;

    public void maybeAdd() {
        dependentTask.doFirst(
                new AddContainersEnvironment(
                        log,
                        projectDescription,
                        containersBuildServiceProvider,
                        dockerContainersExtension.getContainersRequestedByUser()
                )
        );
    }
}
