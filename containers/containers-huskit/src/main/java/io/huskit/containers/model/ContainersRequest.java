package io.huskit.containers.model;

import io.huskit.common.Log;
import io.huskit.containers.integration.ContainerSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class ContainersRequest {

    Log log;
    ProjectDescription projectDescription;
    List<ContainerSpec> requestedContainers;
}
