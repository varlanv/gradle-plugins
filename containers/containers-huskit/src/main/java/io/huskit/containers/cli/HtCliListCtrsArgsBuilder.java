package io.huskit.containers.cli;

import io.huskit.containers.api.HtArg;
import io.huskit.containers.HtDefaultListContainersFilterBuilder;
import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterBuilder;
import io.huskit.containers.api.list.arg.ListCtrsArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@With
@RequiredArgsConstructor
public class HtCliListCtrsArgsBuilder implements HtListContainersArgsBuilder {

    boolean all;
    List<HtListContainersFilter> filters;

    public HtCliListCtrsArgsBuilder() {
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
            result.add(HtArg.of("--filter", filter.getKey() + "=" + filter.getValue()));
        }
        return new ListCtrsArgs(Collections.unmodifiableList(result));
    }

    @Override
    public HtListContainersArgsBuilder all() {
        return this.withAll(true);
    }

    @Override
    public HtListContainersArgsBuilder filter(Function<HtListContainersFilterBuilder, HtListContainersFilter> filter) {
        var newFilter = new ArrayList<>(this.filters);
        newFilter.add(filter.apply(new HtDefaultListContainersFilterBuilder()));
        return this.withFilters(newFilter);
    }
}
