package io.huskit.containers.internal.cli;

import io.huskit.common.HtStrings;
import io.huskit.common.Volatile;
import io.huskit.containers.api.cli.HtArg;
import io.huskit.containers.api.list.HtListContainersFilterType;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.*;

@With
@RequiredArgsConstructor
public class HtCliListCtrsArgsSpec implements HtListContainersArgsSpec {

    Volatile<Boolean> all;
    Map<HtListContainersFilterType, Map.Entry<String, String>> filters;
    Map<String, String> labels;

    public HtCliListCtrsArgsSpec() {
        this(Volatile.of(false), new LinkedHashMap<>(), new LinkedHashMap<>());
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
        this.labels.put(label.toString(), value.toString());
        return this;
    }

    public List<HtArg> build() {
        var all = this.all.require();
        var result = new ArrayList<HtArg>(filters.size());
        if (all) {
            result.add(HtArg.of("-a"));
        }
        for (var psFilter : filters.entrySet()) {
            var filterEntry = psFilter.getValue();
            result.add(HtArg.of("--filter", HtStrings.doubleQuotedParam(filterEntry.getKey(), filterEntry.getValue())));
        }
        for (var label : labels.entrySet()) {
            result.add(HtArg.of("--filter", HtStrings.doubleQuotedParam("label", label.getKey(), label.getValue())));
        }
        result.add(HtArg.of("--format", HtStrings.doubleQuote("{{json .}}")));
        return Collections.unmodifiableList(result);
    }
}
