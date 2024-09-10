package io.huskit.containers.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface ProjectDescription {

    String rootProjectName();

    String path();

    String name();

    @Getter
    @RequiredArgsConstructor
    class Default implements ProjectDescription {

        String rootProjectName;
        String path;
        String name;
    }
}
