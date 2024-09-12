package io.huskit.containers.cli;

import io.huskit.containers.api.HtArg;
import io.huskit.containers.HtDefaultPsFilterBuilder;
import io.huskit.containers.api.ps.HtPsFilter;
import io.huskit.containers.api.ps.HtPsFilterBuilder;
import io.huskit.containers.api.ps.arg.HtListPsArgs;
import io.huskit.containers.api.ps.arg.HtPsArgs;
import io.huskit.containers.api.ps.arg.HtPsArgsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@With
@RequiredArgsConstructor
public class HtCliPsArgsBuilder implements HtPsArgsBuilder {

    boolean all;
    List<HtPsFilter> filters;

    public HtCliPsArgsBuilder() {
        this.all = false;
        this.filters = new ArrayList<>(3);
    }

    @Override
    public HtPsArgs build() {
        var result = new ArrayList<HtArg>(filters.size());
        if (all) {
            result.add(HtArg.of("-a"));
        }
        result.add(HtArg.of("--format", "\"{{json .}}\""));
        for (var psFilter : filters) {
            var filter = psFilter.value();
            result.add(HtArg.of("--filter", filter.getKey() + "=" + filter.getValue()));
        }
        return new HtListPsArgs(Collections.unmodifiableList(result));
    }

    @Override
    public HtPsArgsBuilder all() {
        return this.withAll(true);
    }

    @Override
    public HtPsArgsBuilder filter(Function<HtPsFilterBuilder, HtPsFilter> filter) {
        var newFilter = new ArrayList<>(this.filters);
        newFilter.add(filter.apply(new HtDefaultPsFilterBuilder()));
        return this.withFilters(newFilter);
    }
}
