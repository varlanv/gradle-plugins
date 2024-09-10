package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.log.Log;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class ContainersRequest {

    Log taskLog;
    ProjectDescription projectDescription;
    List<RequestedContainer> requestedContainers;
}
