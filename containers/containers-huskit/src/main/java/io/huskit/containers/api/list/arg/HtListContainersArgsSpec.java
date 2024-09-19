package io.huskit.containers.api.list.arg;

import io.huskit.containers.api.list.HtListContainersFilterSpec;

import java.util.function.Consumer;

public interface HtListContainersArgsSpec {

    HtListContainersArgsSpec withAll();

    HtListContainersArgsSpec withFilter(Consumer<HtListContainersFilterSpec> filter);
}
