package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DefaultRequestedContainers implements RequestedContainers {

    private final List<RequestedContainer> requestedContainers;

    public DefaultRequestedContainers(Collection<RequestedContainer> requestedContainers) {
        this.requestedContainers = List.copyOf(requestedContainers);
    }

    public DefaultRequestedContainers(RequestedContainer requestedContainer) {
        this.requestedContainers = List.of(requestedContainer);
    }

    @Override
    public List<RequestedContainer> list() {
        return requestedContainers;
    }
}
