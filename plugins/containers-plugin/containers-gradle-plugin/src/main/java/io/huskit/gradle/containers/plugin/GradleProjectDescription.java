package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.ProjectDescription;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class GradleProjectDescription implements ProjectDescription, Serializable {

    String rootProjectName;
    String path;
    String name;
}
