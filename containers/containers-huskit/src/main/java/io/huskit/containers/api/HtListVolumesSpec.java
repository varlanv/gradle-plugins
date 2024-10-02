package io.huskit.containers.api;

import java.util.Map;

public interface HtListVolumesSpec {

    HtListVolumesSpec withFilterByDangling(Boolean dangling);

    HtListVolumesSpec withFilterByLabelExists(CharSequence labelKey);

    HtListVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue);

    HtListVolumesSpec withFilterByLabels(Map<String, String> labels);
}
