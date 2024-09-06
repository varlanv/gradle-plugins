package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public final class DefaultRequestedContainers implements RequestedContainers {

    List<RequestedContainer> list;

    public DefaultRequestedContainers(Collection<RequestedContainer> requestedContainers) {
        this.list = List.copyOf(requestedContainers);
    }

    public DefaultRequestedContainers(RequestedContainer requestedContainer) {
        this.list = List.of(requestedContainer);
    }

    @Override
    public Stream<RequestedContainer> stream() {
        return list.stream();
    }

    @Override
    public int size() {
        return list.size();
    }
}
