package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.ContainerLauncher;
import io.huskit.containers.model.started.StartedContainerInternal;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.containers.model.started.StartedContainersInternal;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DockerContainers implements Containers {

    Log log;
    StartedContainersInternal startedContainersInternal;
    RequestedContainers requestedContainers;

    @Override
    public StartedContainers start() {
        return () -> {
            log.info("Requesting containers to start - [{}]", requestedContainers);
            var containers = requestedContainers.list().stream()
                    .map(requestedContainer -> (Supplier<StartedContainerInternal>) () -> startedContainersInternal.startOrCreateAndStart(requestedContainer))
                    .collect(Collectors.toList());
            return new ContainerLauncher(log, containers).start();
        };
    }
}
