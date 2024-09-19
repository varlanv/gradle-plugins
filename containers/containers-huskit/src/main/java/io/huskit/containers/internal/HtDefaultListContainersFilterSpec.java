package io.huskit.containers.internal;

import io.huskit.containers.api.list.HtListContainersFilterSpec;
import io.huskit.containers.api.list.HtListContainersFilterType;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class HtDefaultListContainersFilterSpec implements HtListContainersFilterSpec {

    Map<HtListContainersFilterType, Map.Entry<String, String>> filters = new LinkedHashMap<>();

    @Override
    public HtDefaultListContainersFilterSpec id(CharSequence id) {
        filters.put(HtListContainersFilterType.ID, Map.entry("id", id.toString()));
        return this;
    }

    @Override
    public HtDefaultListContainersFilterSpec name(CharSequence name) {
        filters.put(HtListContainersFilterType.NAME, Map.entry("name", name.toString()));
        return this;
    }

    @Override
    public HtDefaultListContainersFilterSpec label(CharSequence label, CharSequence value) {
        filters.put(HtListContainersFilterType.LABEL, Map.entry("label", label + "=" + value));
        return this;
    }
}
