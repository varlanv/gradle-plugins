package io.huskit.containers.internal.cli;

import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliRmSpec {

    List<String> containerIds;
    Boolean force;
    Boolean volumes;

    public HtCliRmSpec(List<? extends CharSequence> containerIds) {
        this(containerIds.stream().map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }

    public HtCliRmSpec(CharSequence... containerIds) {
        this(Stream.of(containerIds).map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }

    public HtCliRmSpec() {
        this(List.of());
    }

    public List<String> build() {
        var command = new ArrayList<String>(4);
        command.add("docker");
        command.add("rm");
        if (force) {
            command.add("--force");
        }
        if (volumes) {
            command.add("--volumes");
        }
        command.addAll(containerIds);
        return Collections.unmodifiableList(command);
    }
}
