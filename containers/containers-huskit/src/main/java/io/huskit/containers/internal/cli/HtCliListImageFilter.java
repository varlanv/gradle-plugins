package io.huskit.containers.internal.cli;

import io.huskit.containers.api.image.HtListImageFilter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliListImageFilter implements HtListImageFilter {

    HtCliListImages parent;
    HtCliListImagesSpec spec;

    @Override
    public HtCliListImages byBefore(String image) {
        return parent.withSpec(spec.addArgs("--filter", "before=" + image));
    }

    @Override
    public HtCliListImages bySince(String image) {
        return parent.withSpec(spec.addArgs("--filter", "since=" + image));
    }

    @Override
    public HtCliListImages byReference(String reference) {
        return parent.withSpec(spec.addArgs("--filter", "reference=" + reference));
    }

    @Override
    public HtCliListImages byUntil(String image) {
        return parent.withSpec(spec.addArgs("--filter", "until=" + image));
    }

    @Override
    public HtCliListImages byDangling(Boolean dangling) {
        return parent.withSpec(spec.addArgs("--filter", "dangling=" + dangling));
    }

    @Override
    public HtCliListImages byLabel(String key) {
        return parent.withSpec(spec.addArgs("--filter", "label=" + key));
    }

    @Override
    public HtCliListImages byLabel(String key, String value) {
        return parent.withSpec(spec.addArgs("--filter", "label=" + key + "=" + value));
    }
}
