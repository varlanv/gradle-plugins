package io.huskit.containers.internal;

import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterSpec;
import io.huskit.containers.api.list.HtListContainersFilterType;
import io.huskit.containers.internal.cli.HtCliListCtrsFilter;

import java.util.Map;

public class HtDefaultListContainersFilterSpec implements HtListContainersFilterSpec {

    @Override
    public HtListContainersFilter id(CharSequence id) {
        return new HtCliListCtrsFilter(Map.entry("id", id.toString()), HtListContainersFilterType.ID);
    }

    @Override
    public HtListContainersFilter name(CharSequence name) {
        return new HtCliListCtrsFilter(Map.entry("name", name.toString()), HtListContainersFilterType.NAME);
    }

    @Override
    public HtListContainersFilter label(CharSequence label, CharSequence value) {
        return new HtCliListCtrsFilter(Map.entry("label", label + "=" + value), HtListContainersFilterType.LABEL);
    }
}
