package io.huskit.containers.internal.cli;

import io.huskit.containers.api.HtArg;
import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterSpec;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.list.arg.ListCtrsArgs;
import io.huskit.containers.internal.HtDefaultListContainersFilterSpec;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@With
@RequiredArgsConstructor
public class HtCliListCtrsArgsSpec implements HtListContainersArgsSpec {

    boolean all;
    List<HtListContainersFilter> filters;

    public HtCliListCtrsArgsSpec() {
        this.all = false;
        this.filters = new ArrayList<>(3);
    }

    @Override
    public HtListContainersArgs build() {
        var result = new ArrayList<HtArg>(filters.size());
        if (all) {
            result.add(HtArg.of("-a"));
        }
        result.add(HtArg.of("--format", "\"{{json .}}\""));
        for (var psFilter : filters) {
            var filter = psFilter.value();
            result.add(HtArg.of("--filter", "\"" + filter.getKey() + "=" + filter.getValue() + "\""));
        }
        return new ListCtrsArgs(Collections.unmodifiableList(result));
    }

    @Override
    public HtListContainersArgsSpec withAll() {
        return this.withAll(true);
    }

    @Override
    public HtListContainersArgsSpec withFilter(Function<HtListContainersFilterSpec, HtListContainersFilter> filter) {
        var newFilter = new ArrayList<>(this.filters);
        newFilter.add(filter.apply(new HtDefaultListContainersFilterSpec()));
        return this.withFilters(newFilter);
    }
}
