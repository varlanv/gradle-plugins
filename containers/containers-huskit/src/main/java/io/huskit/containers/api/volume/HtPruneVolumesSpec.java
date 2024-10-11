package io.huskit.containers.api.volume;

import java.util.Map;

public interface HtPruneVolumesSpec {

    HtPruneVolumesSpec withAll();

    HtPruneVolumesSpec withFilterByDangling(Boolean dangling);

    HtPruneVolumesSpec withFilterByLabelExists(CharSequence labelKey);

    HtPruneVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue);

    HtPruneVolumesSpec withFilterByLabels(Map<String, String> labels);
}
