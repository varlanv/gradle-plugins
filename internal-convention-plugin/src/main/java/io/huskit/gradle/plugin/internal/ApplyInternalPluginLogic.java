package io.huskit.gradle.plugin.internal;

import io.huskit.gradle.plugin.HuskitInternalConventionExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;

@RequiredArgsConstructor
public class ApplyInternalPluginLogic {

    String projectPath;
    ProviderFactory providers;
    PluginManager pluginManager;
    RepositoryHandler repositories;
    DependencyHandler dependencies;
    ExtensionContainer extensions;
    HuskitInternalConventionExtension huskitConventionExtension;
    SoftwareComponentContainer components;
    TaskContainer tasks;
    ConfigurationContainer configurations;
    Provider<Project> commonTestProject;
    InternalEnvironment environment;
    InternalProperties properties;
    Project project;

    public void apply() {
        var isGradlePlugin = projectPath.startsWith(":plugins") && projectPath.endsWith("-plugin");
        new AddCommonPlugins(
                isGradlePlugin,
                pluginManager
        ).add();
        new ConfigureRepositories(
                repositories,
                environment
        ).configure();
        new ConfigureJava(
                pluginManager,
                extensions
        ).configure();
        new AddCommonDependencies(
                new AddLombokDependencies(
                        pluginManager,
                        properties,
                        dependencies
                ),
                new AddSpockDependencies(
                        pluginManager,
                        properties,
                        dependencies
                ),
                new AddTestDependencies(
                        pluginManager,
                        projectPath,
                        environment,
                        dependencies,
                        commonTestProject,
                        properties
                )
        ).add();
        project.afterEvaluate(evaluated -> {
            new ConfigureTests(
                    extensions,
                    huskitConventionExtension,
                    configurations,
                    pluginManager,
                    tasks
            ).configure();
        });
        new ConfigurePublishing(
                pluginManager,
                extensions,
                components
        ).configure();
    }
}
