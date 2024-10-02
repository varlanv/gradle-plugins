package io.huskit.containers.api;

import io.huskit.common.StringTuples;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HtCliListVolumesSpec implements HtListVolumesSpec {

    StringTuples args;

    public HtCliListVolumesSpec() {
        args = new StringTuples("docker", "volume", "ls", "-q");
    }

    @Override
    public HtListVolumesSpec withFilterByDangling(Boolean dangling) {
        args.add("--filter", "\"dangling=%s\"", dangling.toString());
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabelExists(CharSequence labelKey) {
        args.add("--filter", "\"label=%s\"", labelKey);
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabel(CharSequence labelKey, CharSequence labelValue) {
        args.add("--filter", "\"label=%s=%s\"", labelKey, labelValue);
        return this;
    }

    @Override
    public HtListVolumesSpec withFilterByLabels(Map<String, String> labels) {
        for (var entry : labels.entrySet()) {
            args.add("--filter", "\"label=%s=%s\"", entry.getKey(), Objects.requireNonNull(entry.getValue()));
        }
        return this;
    }

    public List<String> toCommand() {
        return args.toList();
    }
}
