package io.huskit.containers.internal.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.image.HtRmImagesSpec;

import java.util.*;

public class HtCliRmImagesSpec implements HtRmImagesSpec {

    List<String> imageIds;
    Mutable<Boolean> force = Volatile.of(false);
    Mutable<Boolean> noPrune = Volatile.of(false);

    public HtCliRmImagesSpec(Collection<? extends CharSequence> imageIds) {
        if (imageIds.isEmpty()) {
            throw new IllegalArgumentException("Image IDs must not be empty");
        }
        this.imageIds = new ArrayList<>(imageIds.size());
        for (var imageId : imageIds) {
            this.imageIds.add(imageId.toString());
        }
    }

    public HtCliRmImagesSpec(CharSequence... imageIds) {
        this(Arrays.asList(imageIds));
    }

    @Override
    public HtCliRmImagesSpec withForce() {
        this.force.set(true);
        return this;
    }

    @Override
    public HtCliRmImagesSpec withNoPrune() {
        this.noPrune.set(true);
        return this;
    }

    public List<String> toCommand() {
        var command = new ArrayList<String>(4 + imageIds.size());
        command.add("docker");
        command.add("rmi");
        if (force.require()) {
            command.add("--force");
        }
        if (noPrune.require()) {
            command.add("--no-prune");
        }
        command.addAll(imageIds);
        return Collections.unmodifiableList(command);
    }
}
