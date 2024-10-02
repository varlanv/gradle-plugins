package io.huskit.containers.api.image;

public interface HtPullImagesSpec {

    HtPullImagesSpec withAllTags();

    HtPullImagesSpec withDisableContentTrust();

    HtPullImagesSpec withPlatformString(CharSequence platformString);
}
