package io.huskit.containers.api.list;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsBuilder;

import java.util.function.Function;
import java.util.stream.Stream;

public interface HtListContainers {

    HtListContainers withArgs(Function<HtListContainersArgsBuilder, HtListContainersArgs> args);

    Stream<HtContainer> stream();
}
