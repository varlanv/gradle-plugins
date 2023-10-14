package io.huskit.gradle.containers.plugin;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@RequiredArgsConstructor
public class GradleProjectDescription implements ProjectDescription, Serializable {

    private final String path;
    private final String name;

    @Override
    public String path() {
        return path;
    }

    @Override
    public String name() {
        return name;
    }
}
