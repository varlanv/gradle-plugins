package io.huskit.containers.api.list.arg;

import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterSpec;

import java.util.function.Function;

public interface HtListContainersArgsSpec {

    HtListContainersArgs build();

    HtListContainersArgsSpec withAll();

    HtListContainersArgsSpec withFilter(Function<HtListContainersFilterSpec, HtListContainersFilter> filter);
}
