package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.ContainerLauncher;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class DockerContainers implements Containers {

    Log log;
    StartedContainersRegistry startedContainersRegistry;
    RequestedContainers requestedContainers;

    @Override
    public StartedContainers start() {
        return () -> {
            log.info("Requesting containers to start - [{}]", requestedContainers);
            var containers = requestedContainers.list().stream()
                    .map(requestedContainer -> (Supplier<RequestedContainer>) () -> requestedContainer)
                    .collect(Collectors.toList());
            return new ContainerLauncher<RequestedContainer, StartedContainer>(containers)
                    .doParallel(startedContainersRegistry::getOrStart);
        };
    }
}
