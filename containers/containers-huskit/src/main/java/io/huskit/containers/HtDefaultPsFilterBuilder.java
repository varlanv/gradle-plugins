package io.huskit.containers;

import io.huskit.containers.cli.HtCliPsFilter;
import io.huskit.containers.api.ps.HtPsFilter;
import io.huskit.containers.api.ps.HtPsFilterBuilder;
import io.huskit.containers.api.ps.HtPsFilterType;

import java.util.Map;

public class HtDefaultPsFilterBuilder implements HtPsFilterBuilder {

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
