package io.huskit.containers.internal.cli;

import io.huskit.common.StringTuples;

import java.util.List;

public class HtCliListImagesSpec implements HtListImagesSpec {

    StringTuples args;

    public HtCliListImagesSpec() {
        args = new StringTuples("docker", "images", "-q");
    }

    @Override
    public HtCliListImagesSpec withAll() {
        args.add("--all");
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByBefore(String image) {
        args.add("--filter", "\"before=%s\"", image);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterBySince(String image) {
        args.add("--filter", "\"since=%s\"", image);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByReference(String reference) {
        args.add("--filter", "\"reference=%s\"", reference);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByUntil(String image) {
        args.add("--filter", "\"until=%s\"", image);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByDangling(Boolean dangling) {
        args.add("--filter", "dangling=" + dangling);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key) {
        args.add("--filter", "\"label=%s\"", key);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key, String value) {
        args.add("--filter", "\"label=%s=%s\"", key, value);
        return this;
    }

    List<String> toCommand() {
        return args.toList();
    }
}
