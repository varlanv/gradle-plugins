package io.huskit.containers.cli;

import io.huskit.containers.api.ps.HtPsFilter;
import io.huskit.containers.api.ps.HtPsFilterBuilder;
import io.huskit.containers.api.ps.HtPsFilterType;

import java.util.Map;

class HtCliPsFilterBuilder implements HtPsFilterBuilder {

    static final HtPsFilterBuilder INSTANCE = new HtCliPsFilterBuilder();

    @Override
    public HtPsFilter id(CharSequence id) {
        return new HtCliPsFilter(Map.entry("id", id.toString()), HtPsFilterType.ID);
    }

    @Override
    public HtPsFilter name(CharSequence name) {
        return new HtCliPsFilter(Map.entry("name", name.toString()), HtPsFilterType.NAME);
    }

    @Override
    public HtPsFilter label(CharSequence label) {
        return new HtCliPsFilter(Map.entry("label", label.toString()), HtPsFilterType.LABEL);
    }
}
