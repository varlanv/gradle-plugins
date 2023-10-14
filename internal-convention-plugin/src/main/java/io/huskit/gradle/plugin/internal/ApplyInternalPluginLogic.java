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

    private final String projectPath;
    private final ProviderFactory providers;
    private final PluginManager pluginManager;
    private final RepositoryHandler repositories;
    private final DependencyHandler dependencies;
    private final ExtensionContainer extensions;
    private final HuskitInternalConventionExtension huskitConventionExtension;
    private final SoftwareComponentContainer components;
    private final TaskContainer tasks;
    private final ConfigurationContainer configurations;
    private final Provider<Project> commonTestProject;
    private final InternalEnvironment environment;
    private final InternalProperties properties;
    private final Project project;

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
                new AddCommonTestDependency(
                        pluginManager,
                        projectPath,
                        environment,
                        dependencies,
                        commonTestProject
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
