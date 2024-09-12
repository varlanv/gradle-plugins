package io.huskit.containers.api.list.arg;

import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterBuilder;

import java.util.function.Function;

public interface HtListContainersArgsBuilder {

    HtListContainersArgs build();

    HtListContainersArgsBuilder all();

    HtListContainersArgsBuilder filter(Function<HtListContainersFilterBuilder, HtListContainersFilter> filter);
}
