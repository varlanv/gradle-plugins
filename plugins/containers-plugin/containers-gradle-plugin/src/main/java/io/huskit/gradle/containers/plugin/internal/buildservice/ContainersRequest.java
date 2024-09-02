package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.gradle.containers.plugin.ProjectDescription;
import io.huskit.log.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContainersRequest {

    ProjectDescription projectDescription;
    RequestedContainers requestedContainers;
    Log taskLog;
}
