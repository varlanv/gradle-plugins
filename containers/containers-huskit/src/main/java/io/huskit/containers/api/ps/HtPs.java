package io.huskit.containers.api.ps;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.ps.arg.HtPsArgs;
import io.huskit.containers.api.ps.arg.HtPsArgsBuilder;

import java.util.function.Function;
import java.util.stream.Stream;

public interface HtPs {

    HtPs withArgs(Function<HtPsArgsBuilder, HtPsArgs> args);

    Stream<HtContainer> stream();
}
