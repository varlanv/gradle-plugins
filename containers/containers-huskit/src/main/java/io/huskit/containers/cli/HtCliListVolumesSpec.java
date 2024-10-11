package io.huskit.containers.cli;

import io.huskit.common.HtStrings;
import io.huskit.common.StringTuples;
import io.huskit.containers.api.volume.HtListVolumesSpec;

import java.util.List;
import java.util.Map;

class HtCliListVolumesSpec implements HtListVolumesSpec {

    StringTuples args;

    public HtCliListVolumesSpec() {
        args = new StringTuples("docker", "volume", "ls", "-q");
    }

    @Override
    public HtListVolumesSpec withFilterByDangling(Boolean dangling) {
        args.add("--filter", HtStrings.doubleQuotedParam("dangling", dangling));
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabelExists(CharSequence labelKey) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", labelKey));
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", labelKey, labelValue));
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabels(Map<String, String> labels) {
        for (var entry : labels.entrySet()) {
            args.add("--filter", HtStrings.doubleQuotedParam("label", entry.getKey(), entry.getValue()));
        }
        return this;
    }

    public List<String> toCommand() {
        return args.toList();
    }
}
