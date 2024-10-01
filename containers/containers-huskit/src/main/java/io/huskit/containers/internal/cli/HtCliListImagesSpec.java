package io.huskit.containers.internal.cli;

import io.huskit.common.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class HtCliListImagesSpec implements HtListImagesSpec {

    LinkedHashSet<Tuple<String, String>> args;

    public HtCliListImagesSpec() {
        this.args = new LinkedHashSet<>(11);
        addArg("docker", "images");
        addArg("-q");
    }

    @Override
    public HtCliListImagesSpec withAll() {
        return addArg("--all");
    }

    @Override
    public HtCliListImagesSpec withFilterByBefore(String image) {
        return addArg("--filter", String.format("\"before=%s\"", image));
    }

    @Override
    public HtCliListImagesSpec withFilterBySince(String image) {
        return addArg("--filter", String.format("\"since=%s\"", image));
    }

    @Override
    public HtCliListImagesSpec withFilterByReference(String reference) {
        return addArg("--filter", String.format("\"reference=%s\"", reference));
    }

    @Override
    public HtCliListImagesSpec withFilterByUntil(String image) {
        return addArg("--filter", String.format("\"until=%s\"", image));
    }

    @Override
    public HtCliListImagesSpec withFilterByDangling(Boolean dangling) {
        return addArg("--filter", "dangling=" + dangling);
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key) {
        return addArg("--filter", String.format("\"label=%s\"", key));
    }

    @Override
    public HtCliListImagesSpec withFilterByLabel(String key, String value) {
        return addArg("--filter", String.format("\"label=%s=%s\"", key, value));
    }

    List<String> toCommand() {
        var command = new ArrayList<String>(args.size() * 2);
        for (var arg : args) {
            command.add(arg.left());
            if (!arg.right().isEmpty()) {
                command.add(arg.right());
            }
        }
        return command;
    }

    private HtCliListImagesSpec addArg(String arg) {
        this.args.add(Tuple.of(arg, ""));
        return this;
    }

    private HtCliListImagesSpec addArg(String key, String value) {
        this.args.add(Tuple.of(key, value));
        return this;
    }
}
