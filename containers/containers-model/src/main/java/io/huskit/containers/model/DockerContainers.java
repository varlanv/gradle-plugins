package io.huskit.containers.model;

import io.huskit.common.concurrent.ParallelFnRunner;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class DockerContainers implements Containers {

    Log log;
    StartedContainersRegistry startedContainersRegistry;
    RequestedContainers requestedContainers;

    @Override
    public StartedContainers start() {
        return this::getStartedContainers;
    }

    private List<StartedContainer> getStartedContainers() {
        log.info("Requesting [{}] containers to start", requestedContainers.size());
        var containers = requestedContainers.stream()
                .map(requestedContainer -> (Supplier<RequestedContainer>) () -> requestedContainer)
                .collect(Collectors.toList());
        return new ParallelFnRunner<RequestedContainer, StartedContainer>(containers)
                .doParallel(startedContainersRegistry::getOrStart);
    }
}
