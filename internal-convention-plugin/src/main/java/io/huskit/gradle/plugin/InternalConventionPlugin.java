package io.huskit.gradle.plugin;

import io.huskit.gradle.plugin.internal.ApplyInternalPluginLogic;
import io.huskit.gradle.plugin.internal.InternalEnvironment;
import io.huskit.gradle.plugin.internal.InternalProperties;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Objects;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InternalConventionPlugin implements Plugin<Project> {

    ProviderFactory providers;

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var environment = (InternalEnvironment) Objects.requireNonNullElseGet(
                extensions.findByName(InternalEnvironment.EXTENSION_NAME),
                () -> new InternalEnvironment(
                        providers.environmentVariable("CI").isPresent(),
                        false
                ));
        var properties = (InternalProperties) Objects.requireNonNullElseGet(
                extensions.findByName(InternalProperties.EXTENSION_NAME),
                () -> new InternalProperties(((VersionCatalogsExtension) extensions.getByName("versionCatalogs")).named("libs")));
        var huskitConventionExtension = (HuskitInternalConventionExtension) Objects.requireNonNullElseGet(
                extensions.findByName(HuskitInternalConventionExtension.EXTENSION_NAME),
                () -> extensions.create(HuskitInternalConventionExtension.EXTENSION_NAME, HuskitInternalConventionExtension.class));
        huskitConventionExtension.getIntegrationTestName().convention("integrationTest");
        new ApplyInternalPluginLogic(
                project.getPath(),
                providers,
                project.getPluginManager(),
                project.getRepositories(),
                project.getDependencies(),
                extensions,
                huskitConventionExtension,
                project.getComponents(),
                project.getTasks(),
                project.getConfigurations(),
                providers.provider(() -> project.project(":common-test")),
                environment,
                properties,
                project
        ).apply();
    }
}
