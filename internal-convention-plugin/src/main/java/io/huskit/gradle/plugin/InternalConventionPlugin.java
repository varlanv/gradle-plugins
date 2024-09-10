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

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class InternalConventionPlugin implements Plugin<Project> {

    ProviderFactory providers;

    @Override
    public void apply(Project project) {
        var extensions = project.getExtensions();
        var environment = (InternalEnvironment) extensions.findByName(InternalEnvironment.EXTENSION_NAME);
        if (environment == null) {
            var ci = providers.environmentVariable("CI").isPresent();
            environment = new InternalEnvironment(ci, false);
        }
        var properties = (InternalProperties) extensions.findByName(InternalProperties.EXTENSION_NAME);
        if (properties == null) {
            properties = new InternalProperties(
                    ((VersionCatalogsExtension) extensions.getByName("versionCatalogs")).named("libs")
            );
        }
        var huskitConventionExtension = (HuskitInternalConventionExtension) extensions.findByName(HuskitInternalConventionExtension.EXTENSION_NAME);
        if (huskitConventionExtension == null) {
            huskitConventionExtension = extensions.create(HuskitInternalConventionExtension.EXTENSION_NAME, HuskitInternalConventionExtension.class);
        }
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
