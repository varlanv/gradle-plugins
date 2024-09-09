package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.log.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.ListProperty;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class ContainersServiceRequest {

    Log taskLog;
    ProjectDescription projectDescription;
    ListProperty<ContainerRequestSpec> requestSpec;
    @Nullable
    TestContainersDelegate testContainersDelegate;
}
