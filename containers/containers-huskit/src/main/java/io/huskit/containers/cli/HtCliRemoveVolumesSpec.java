package io.huskit.containers.cli;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@RequiredArgsConstructor
class HtCliRemoveVolumesSpec {

    Boolean force;
    Iterable<? extends CharSequence> volumeIds;

    public Set<String> toCommand() {
        var command = new LinkedHashSet<String>();
        command.add("docker");
        command.add("volume");
        command.add("rm");
        if (force) {
            command.add("--force");
        }
        var prevSize = command.size();
        for (var volumeId : volumeIds) {
            var vlmId = volumeId.toString();
            if (vlmId.isBlank()) {
                throw new IllegalArgumentException("Volume ID cannot be blank");
            }
            command.add(vlmId);
        }
        if (command.size() == prevSize) {
            throw new IllegalArgumentException("Received empty volume ID list");
        }
        return command;
    }
}
