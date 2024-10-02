package io.huskit.containers.api;

import io.huskit.common.StringTuples;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HtCliPruneVolumesSpec implements HtPruneVolumesSpec {

    StringTuples args;

    public HtCliPruneVolumesSpec() {
        args = new StringTuples("docker", "volume", "prune", "-f");
    }

    @Override
    public HtCliPruneVolumesSpec withAll() {
        args.add("--all");
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByDangling(Boolean dangling) {
        args.add("--filter", "\"dangling=%s\"", dangling.toString());
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabelExists(CharSequence labelKey) {
        args.add("--filter", "\"label=%s\"", labelKey.toString());
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue) {
        args.add("--filter", "\"label=%s=%s\"", labelKey.toString(), labelValue.toString());
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabels(Map<String, String> labels) {
        labels.forEach((key, value) -> {
                    args.add("--filter", "\"label=%s=%s\"",
                            key,
                            Objects.requireNonNull(value, "Null label values are not allowed"));
                }
        );
        return this;
    }

    public List<String> toCommand() {
        return args.toList();
    }
}
