package io.huskit.containers.api.list;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HtListContainers {

    HtListContainers withArgs(Function<HtListContainersArgsSpec, HtListContainersArgs> args);

    Stream<HtContainer> asStream();

    default List<HtContainer> asList() {
        return asStream().collect(Collectors.toList());
    }
}
