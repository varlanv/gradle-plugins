package io.huskit.containers.api.image;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;

import java.util.ArrayList;
import java.util.List;

public class HtCliPullImagesSpec implements HtPullImagesSpec {

    String imageId;
    Mutable<Boolean> allTags = Volatile.of(false);
    Mutable<Boolean> disableContentTrust = Volatile.of(false);
    Mutable<String> platform = Volatile.of();

    public HtCliPullImagesSpec(CharSequence imageId) {
        this.imageId = imageId.toString();
        if (this.imageId.isBlank()) {
            throw new IllegalArgumentException("Image ID cannot be blank");
        }
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
        var command = new ArrayList<String>(4 + imageId.length());
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
        command.add(imageId);
        return command;
    }
}
