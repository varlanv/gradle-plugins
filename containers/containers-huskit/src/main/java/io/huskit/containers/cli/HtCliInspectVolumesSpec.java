package io.huskit.containers.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HtCliInspectVolumesSpec {

    List<String> volumeIds;

    HtCliInspectVolumesSpec(CharSequence volumeId) {
        this(Collections.singletonList(volumeId));
    }

    public HtCliInspectVolumesSpec(Iterable<? extends CharSequence> volumeIds) {
        var volumeIdsList = new ArrayList<String>(5);
        for (var volumeId : volumeIds) {
            var vlmId = volumeId.toString();
            if (vlmId.isBlank()) {
                throw new IllegalArgumentException("Volume ID cannot be blank");
            }
            volumeIdsList.add(vlmId);
        }
        this.volumeIds = volumeIdsList;
    }

    public List<String> toCommand() {
        var command = new ArrayList<String>(volumeIds.size() + 3);
        command.add("docker");
        command.add("volume");
        command.add("inspect");
        command.add("--format=\"{{json .}}\"");
        command.addAll(volumeIds);
        return command;
    }
}
