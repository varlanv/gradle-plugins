package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

@RequiredArgsConstructor
public class ConfigureRepositories {

    RepositoryHandler repositories;
    InternalEnvironment environment;

    public void configure() {
        if (environment.isLocal()) {
            repositories.add(repositories.mavenLocal());
        }
        repositories.add(repositories.mavenCentral());
    }
}
