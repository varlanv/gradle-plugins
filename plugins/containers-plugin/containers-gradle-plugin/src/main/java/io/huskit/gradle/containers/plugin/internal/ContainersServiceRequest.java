package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import io.huskit.containers.integration.HtIntegratedDocker;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.ListProperty;

@Getter
@RequiredArgsConstructor
public final class ContainersServiceRequest {

    Log taskLog;
    ProjectDescription projectDescription;
    ListProperty<ContainerRequestSpec> requestSpec;
    HtIntegratedDocker integratedDocker;
}
