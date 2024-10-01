package io.huskit.containers.internal.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;

import java.util.*;

public class HtCliRmSpec implements HtRmSpec {

    List<String> containerIds;
    Mutable<Boolean> force = Volatile.of(false);
    Mutable<Boolean> volumes = Volatile.of(false);

    public HtCliRmSpec(Collection<? extends CharSequence> containerIds) {
        if (containerIds.isEmpty()) {
            throw new IllegalArgumentException("Container IDs must not be empty");
        }
        this.containerIds = new ArrayList<>(containerIds.size());
        for (var containerId : containerIds) {
            this.containerIds.add(containerId.toString());
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
