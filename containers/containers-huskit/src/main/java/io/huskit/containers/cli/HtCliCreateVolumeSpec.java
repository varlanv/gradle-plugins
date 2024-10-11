package io.huskit.containers.cli;

import io.huskit.common.HtStrings;
import io.huskit.common.StringTuples;
import io.huskit.containers.api.volume.HtCreateVolumeSpec;

import java.util.List;
import java.util.Map;

class HtCliCreateVolumeSpec implements HtCreateVolumeSpec {

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
        args.add("--opt", HtStrings.doubleQuotedParam(driverOptsKey, driverOptsValue));
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withLabel(CharSequence label) {
        args.add("--label", label.toString());
        return this;
    }

    @Override
    public HtCliCreateVolumeSpec withLabel(CharSequence key, CharSequence value) {
        args.add("--label", HtStrings.doubleQuotedParam(key, value));
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
