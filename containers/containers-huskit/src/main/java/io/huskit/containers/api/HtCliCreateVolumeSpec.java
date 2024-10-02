package io.huskit.containers.api;

import io.huskit.common.StringTuples;

import java.util.List;
import java.util.Map;

public class HtCliCreateVolumeSpec implements HtCreateVolumeSpec {

    String volumeName;
    StringTuples args;

    public HtCliCreateVolumeSpec() {
        this("");
    }

    public HtCliCreateVolumeSpec(CharSequence volumeName) {
        this.volumeName = volumeName.toString();
        this.args = new StringTuples("docker", "volume", "create");
    }

    @Override
    public HtCliCreateVolumeSpec withDriver(CharSequence driver) {
        args.add("--driver", driver);
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withDriverOpts(CharSequence driverOptsKey, CharSequence driverOptsValue) {
        args.add("--opt", "\"%s=%s\"", driverOptsKey.toString(), driverOptsValue.toString());
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withLabel(CharSequence label) {
        args.add("--label", label.toString());
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withLabel(CharSequence key, CharSequence value) {
        args.add("--label", "\"%s=%s\"", key.toString(), value.toString());
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withLabels(Map<String, String> labels) {
        labels.forEach(this::withLabel);
        return this;
    }

    public List<String> toCommand() {
        return args.toList(volumeName);
    }
}
