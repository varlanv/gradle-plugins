package io.huskit.containers.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.api.image.HtRmImagesSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HtCliRmImagesSpec implements HtRmImagesSpec {

    List<HtImgName> imageNames;
    Mutable<Boolean> force = Volatile.of(false);
    Mutable<Boolean> noPrune = Volatile.of(false);

    public HtCliRmImagesSpec(List<HtImgName> imageRefs) {
        if (imageRefs.isEmpty()) {
            throw new IllegalArgumentException("Image references must not be empty");
        }
        this.imageNames = imageRefs;
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
        var command = new ArrayList<String>(4 + imageNames.size());
        command.add("docker");
        command.add("rmi");
        if (force.require()) {
            command.add("--force");
        }
        if (noPrune.require()) {
            command.add("--no-prune");
        }
        for (var imageName : imageNames) {
            command.add(imageName.reference());
        }
        return Collections.unmodifiableList(command);
    }
}
