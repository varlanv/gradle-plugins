package io.huskit.gradle.containers.plugin.api;

public interface FixedContainerPortSpecView {

    void hostValue(Integer value);

    void hostRange(Integer start, Integer end);

    void containerValue(Integer value);
}
