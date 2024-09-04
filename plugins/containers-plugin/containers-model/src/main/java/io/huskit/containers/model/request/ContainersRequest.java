package io.huskit.containers.model.request;

import io.huskit.containers.model.ProjectDescription;
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
