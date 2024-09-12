package io.huskit.containers.api.list;

import java.util.Map;

public interface HtListContainersFilter {

    Map.Entry<String, String> value();

    HtListContainersFilterType type();
}
