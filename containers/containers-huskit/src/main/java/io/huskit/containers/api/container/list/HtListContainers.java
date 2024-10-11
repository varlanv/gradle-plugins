package io.huskit.containers.api.container.list;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.list.arg.HtListContainersArgsSpec;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HtListContainers {

    HtListContainers withArgs(Consumer<HtListContainersArgsSpec> args);

    Stream<HtContainer> asStream();

    default List<HtContainer> asList() {
        return asStream().collect(Collectors.toList());
    }
}
