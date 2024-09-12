package io.huskit.containers.api.ps.arg;

import io.huskit.containers.api.ps.HtPsFilter;
import io.huskit.containers.api.ps.HtPsFilterBuilder;

import java.util.function.Function;

public interface HtPsArgsBuilder {

    HtPsArgs build();

    HtPsArgsBuilder all();

    HtPsArgsBuilder filter(Function<HtPsFilterBuilder, HtPsFilter> filter);
}
