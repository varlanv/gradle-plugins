package io.huskit.containers.internal.cli;

import io.huskit.common.StringTuples;
import io.huskit.common.HtStrings;

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
        args.add("--filter", HtStrings.doubleQuotedParam("before", image), image);
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterBySince(String image) {
        args.add("--filter", HtStrings.doubleQuotedParam("since", image));
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByReference(String reference) {
        args.add("--filter", HtStrings.doubleQuotedParam("reference", reference));
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByUntil(String image) {
        args.add("--filter", HtStrings.doubleQuotedParam("until", image));
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByDangling(Boolean dangling) {
        args.add("--filter", HtStrings.doubleQuotedParam("dangling", dangling));
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", key));
        return this;
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key, String value) {
        args.add("--filter", HtStrings.doubleQuotedParam("label", key, value));
        return this;
    }

    List<String> toCommand() {
        return args.toList();
    }
}
