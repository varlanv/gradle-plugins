package io.huskit.containers.http;

import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.api.image.HtPullImagesSpec;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class HtHttpPullImagesSpec implements HtPullImagesSpec, HtUrl {

    HtImgName imgName;

    @Override
    public HtPullImagesSpec withAllTags() {
        return null;
    }

    @Override
    public HtPullImagesSpec withDisableContentTrust() {
        return null;
    }

    @Override
    public HtPullImagesSpec withPlatformString(CharSequence platformString) {
        return null;
    }

    @Override
    public String url() {
        return "/images/create?fromImage=" + imgName.reference();
    }
}
