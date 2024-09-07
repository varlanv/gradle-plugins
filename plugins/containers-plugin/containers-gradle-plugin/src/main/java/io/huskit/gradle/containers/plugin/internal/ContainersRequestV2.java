package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpec;
import io.huskit.log.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.ListProperty;

@Getter
@RequiredArgsConstructor
public final class ContainersRequestV2 {

    Log taskLog;
    ProjectDescription projectDescription;
    ListProperty<ContainerRequestSpec> requestSpec;
}
