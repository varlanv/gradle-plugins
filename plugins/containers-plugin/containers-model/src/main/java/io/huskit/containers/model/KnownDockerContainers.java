package io.huskit.containers.model;

import io.huskit.containers.model.exception.UnknownContainerTypeException;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.started.StartedContainerInternal;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class KnownDockerContainers {

    Log log;
    Map<ContainerType, Function<RequestedContainer, StartedContainerInternal>> knownContainers;

    public StartedContainerInternal prepareForStart(RequestedContainer requestedContainer) {
        var containerType = requestedContainer.containerType();
        return Optional.ofNullable(knownContainers.get(containerType))
                .map(fn -> {
                    log.info("Starting container of type [{}]", containerType);
                    return fn.apply(requestedContainer);
                })
                .orElseThrow(() -> new UnknownContainerTypeException(containerType));
    }
}
