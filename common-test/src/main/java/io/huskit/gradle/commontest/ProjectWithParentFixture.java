package io.huskit.gradle.commontest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import java.io.File;

@Getter
@RequiredArgsConstructor
public class ProjectWithParentFixture {

    Project project;
    Project parentProject;
    File projectDir;
    File parentProjectDir;
    ObjectFactory objects;
    ProviderFactory providers;
}
