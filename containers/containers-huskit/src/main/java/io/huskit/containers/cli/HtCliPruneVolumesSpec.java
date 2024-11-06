package io.huskit.containers.cli;

import io.huskit.common.HtStrings;
import io.huskit.common.StringTuples;
import io.huskit.containers.api.volume.HtPruneVolumesSpec;

import java.util.List;
import java.util.Map;
import java.util.Objects;

class HtCliPruneVolumesSpec implements HtPruneVolumesSpec {

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
        args.add("--filter", HtStrings.doubleQuotedParam("dangling", dangling));
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabelExists(CharSequence labelKey) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", labelKey));
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", labelKey, labelValue));
        return this;
    }

    @Override
    public HtCliPruneVolumesSpec withFilterByLabels(Map<String, String> labels) {
        labels.forEach((key, value) -> {
                args.add("--filter", HtStrings.doubleQuotedParam("label",
                    key,
                    Objects.requireNonNull(value, "Null label values are not allowed"))
                );
            }
        );
        return this;
    }

    public List<String> toCommand() {
        return args.toList();
    }
}
