package io.huskit.containers.model.request;

import java.util.stream.Stream;

public interface RequestedContainers {

    Stream<RequestedContainer> stream();

    int size();
}
