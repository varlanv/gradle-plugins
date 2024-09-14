package io.huskit.containers.internal.cli;

import io.huskit.containers.api.list.HtListContainersFilter;
import io.huskit.containers.api.list.HtListContainersFilterType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class HtCliListCtrsFilter implements HtListContainersFilter {

    Map.Entry<String, String> value;
    HtListContainersFilterType type;
}
