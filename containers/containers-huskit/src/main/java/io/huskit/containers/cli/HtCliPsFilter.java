package io.huskit.containers.cli;

import io.huskit.containers.api.ps.HtPsFilter;
import io.huskit.containers.api.ps.HtPsFilterType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class HtCliPsFilter implements HtPsFilter {

    Map.Entry<String, String> value;
    HtPsFilterType type;
}
