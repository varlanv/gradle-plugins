package io.huskit.containers.internal.cli;

import io.huskit.common.collection.HtCollections;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;

@RequiredArgsConstructor
public class HtCliListImagesSpec {

    List<String> args;

    public HtCliListImagesSpec() {
        this(List.of());
    }

    public List<String> toCommand() {
        return HtCollections.add("docker", "images", List.copyOf(new LinkedHashSet<>(args)));
    }

    public HtCliListImagesSpec addArgs(String... args) {
        return new HtCliListImagesSpec(HtCollections.add(this.args, args));
    }

    public HtCliListImagesSpec addArg(String arg) {
        return new HtCliListImagesSpec(HtCollections.add(this.args, arg));
    }
}
