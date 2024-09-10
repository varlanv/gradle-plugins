package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.log.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ContainersRequest {

    Log taskLog;
    ProjectDescription projectDescription;
    RequestedContainers requestedContainers;
}
