package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;

@RequiredArgsConstructor
public class ConfigurePublishing {

    PluginManager pluginManager;
    ExtensionContainer extensions;
    SoftwareComponentContainer components;

    public void configure() {
        pluginManager.withPlugin("maven-publish", plugin -> {
            extensions.configure(PublishingExtension.class, publishingExtension -> {
                publishingExtension.getPublications().create("mavenJava", MavenPublication.class, mavenPublication -> {
                    SoftwareComponent javaComponent = components.getByName("java");
                    mavenPublication.from(javaComponent);
                    mavenPublication.versionMapping(versionMappingStrategy -> {
                        versionMappingStrategy.usage("java-api", variantVersionMappingStrategy ->
                                variantVersionMappingStrategy.fromResolutionOf("runtimeClasspath"));
                        versionMappingStrategy.usage("java-runtime", VariantVersionMappingStrategy::fromResolutionResult);
                    });
                });
            });
        });
    }
}
