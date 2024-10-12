package io.huskit.containers.api.container.list;

import io.huskit.containers.api.container.HtContainer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HtListContainers {

    Stream<HtContainer> asStream();

    default List<HtContainer> asList() {
        return asStream().collect(Collectors.toList());
    }
}
