package io.huskit.containers.internal.cli;

import io.huskit.common.Volatile;
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
import java.util.function.Consumer;

@With
@RequiredArgsConstructor
public class HtCliListCtrsArgsSpec implements HtListContainersArgsSpec {

    Volatile<Boolean> all;
    Volatile<List<HtListContainersFilter>> filters;

    public HtCliListCtrsArgsSpec() {
        this.all = Volatile.of(false);
        this.filters = Volatile.of(new ArrayList<>(3));
    }

    public HtListContainersArgs build() {
        var all = this.all.require();
        var filters = this.filters.require();
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
        this.all.set(true);
        return this;
    }

    @Override
    public HtListContainersArgsSpec withFilter(Consumer<HtListContainersFilterSpec> filter) {
        var spec = new HtDefaultListContainersFilterSpec();
        filter.accept(spec);
        spec.filters().forEach((type, entry) -> filters.require().add(new HtCliListCtrsFilter(entry, type)));
        return this;
    }
}
