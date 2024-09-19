package io.huskit.containers.internal.cli;

import io.huskit.common.Volatile;
import io.huskit.containers.api.HtArg;
import io.huskit.containers.api.list.HtListContainersFilterType;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.list.arg.ListCtrsArgs;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@With
@RequiredArgsConstructor
public class HtCliListCtrsArgsSpec implements HtListContainersArgsSpec {

    Volatile<Boolean> all;
    Map<HtListContainersFilterType, Map.Entry<String, String>> filters;

    public HtCliListCtrsArgsSpec() {
        this.all = Volatile.of(false);
        this.filters = new LinkedHashMap<>();
    }

    public HtListContainersArgs build() {
        var all = this.all.require();
        var result = new ArrayList<HtArg>(filters.size());
        if (all) {
            result.add(HtArg.of("-a"));
        }
        result.add(HtArg.of("--format", "\"{{json .}}\""));
        for (var psFilter : filters.entrySet()) {
            var filterEntry = psFilter.getValue();
            result.add(HtArg.of("--filter", "\"" + filterEntry.getKey() + "=" + filterEntry.getValue() + "\""));
        }
        return new ListCtrsArgs(Collections.unmodifiableList(result));
    }

    @Override
    public HtCliListCtrsArgsSpec withAll() {
        this.all.set(true);
        return this;
    }

    @Override
    public HtCliListCtrsArgsSpec withIdFilter(CharSequence id) {
        filters.put(HtListContainersFilterType.ID, Map.entry("id", id.toString()));
        return this;
    }

    @Override
    public HtCliListCtrsArgsSpec withNameFilter(CharSequence name) {
        filters.put(HtListContainersFilterType.NAME, Map.entry("name", name.toString()));
        return this;
    }

    @Override
    public HtCliListCtrsArgsSpec withLabelFilter(CharSequence label, CharSequence value) {
        filters.put(HtListContainersFilterType.LABEL, Map.entry("label", label + "=" + value));
        return this;
    }
}
