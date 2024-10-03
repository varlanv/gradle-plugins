package io.huskit.containers.api.image;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.HtImgName;

import java.util.ArrayList;
import java.util.List;

public class HtCliPullImagesSpec implements HtPullImagesSpec {

    HtImgName imageName;
    Mutable<Boolean> allTags = Volatile.of(false);
    Mutable<Boolean> disableContentTrust = Volatile.of(false);
    Mutable<String> platform = Volatile.of();

    public HtCliPullImagesSpec(HtImgName imageName) {
        this.imageName = imageName;
    }

    @Override
    public HtCliPullImagesSpec withAllTags() {
        this.allTags.set(true);
        return this;
    }

    @Override
    public HtCliPullImagesSpec withDisableContentTrust() {
        this.disableContentTrust.set(true);
        return this;
    }

    @Override
    public HtCliPullImagesSpec withPlatformString(CharSequence platformString) {
        this.platform.set(platformString.toString());
        return this;
    }

    public List<String> toCommand() {
        var command = new ArrayList<String>(4);
        command.add("docker");
        command.add("pull");
        if (allTags.require()) {
            command.add("--all-tags");
        }
        if (disableContentTrust.require()) {
            command.add("--disable-content-trust");
        }

        platform.ifPresent(platform -> {
            if (!platform.isBlank()) {
                command.add("--platform");
                command.add(platform);
            }
        });
        command.add(imageName.reference());
        return command;
    }
}
