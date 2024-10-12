package io.huskit.containers.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.container.run.HtRmSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class HtCliRmSpec implements HtRmSpec {

    List<String> containerIds;
    Mutable<Boolean> force = Volatile.of(false);
    Mutable<Boolean> volumes = Volatile.of(false);

    public HtCliRmSpec(Iterable<? extends CharSequence> containerIds) {
        this.containerIds = new ArrayList<>(5);
        for (var containerId : containerIds) {
            this.containerIds.add(containerId.toString());
        }
        if (this.containerIds.isEmpty()) {
            throw new IllegalArgumentException("Container IDs must not be empty");
        }
    }

    public HtCliRmSpec(CharSequence... containerIds) {
        this(Arrays.asList(containerIds));
    }

    @Override
    public HtCliRmSpec withForce(Boolean force) {
        this.force.set(force);
        return this;
    }

    @Override
    public HtCliRmSpec withForce() {
        return withForce(true);
    }

    @Override
    public HtCliRmSpec withVolumes(Boolean volumes) {
        this.volumes.set(volumes);
        return this;
    }

    @Override
    public HtCliRmSpec withVolumes() {
        return withVolumes(true);
    }

    public List<String> toCommand() {
        var command = new ArrayList<String>(4);
        command.add("docker");
        command.add("rm");
        if (force.require()) {
            command.add("--force");
        }
        if (volumes.require()) {
            command.add("--volumes");
        }
        command.addAll(containerIds);
        return Collections.unmodifiableList(command);
    }
}
