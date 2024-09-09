package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import io.huskit.log.Log;
import lombok.Getter;
import org.gradle.api.provider.ListProperty;
import org.jetbrains.annotations.Nullable;

@Getter
public final class ContainersServiceRequest {

    Log taskLog;
    ProjectDescription projectDescription;
    ListProperty<ContainerRequestSpecView> requestSpec;
    TestContainersDelegate testContainersDelegate;

    public ContainersServiceRequest(Log taskLog, ProjectDescription projectDescription, ListProperty<ContainerRequestSpecView> requestSpec, @Nullable TestContainersDelegate testContainersDelegate) {
        this.taskLog = taskLog;
        this.projectDescription = projectDescription;
        this.requestSpec = requestSpec;
        this.testContainersDelegate = testContainersDelegate;
    }

    public ContainersServiceRequest(Log taskLog, ProjectDescription projectDescription, ListProperty<ContainerRequestSpecView> requestSpec) {
        this(taskLog, projectDescription, requestSpec, null);
    }
}
