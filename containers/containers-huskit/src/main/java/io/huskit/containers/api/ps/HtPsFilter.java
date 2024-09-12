package io.huskit.containers.api.ps;

import java.util.Map;

public interface HtPsFilter {

    Map.Entry<String, String> value();

    HtPsFilterType type();
}
