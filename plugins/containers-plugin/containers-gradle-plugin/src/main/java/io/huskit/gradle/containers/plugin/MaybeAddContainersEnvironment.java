package io.huskit.gradle.containers.plugin;

import io.huskit.gradle.containers.plugin.internal.AddContainersEnvironment;
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension;
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
    DockerContainersExtension dockerContainersExtension;

    public void maybeAdd() {
        log.info("Adding containers environment to task: [{}]", dependentTask.getName());
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
