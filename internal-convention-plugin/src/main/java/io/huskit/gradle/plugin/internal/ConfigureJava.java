package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmVendorSpec;

@RequiredArgsConstructor
public class ConfigureJava {

    PluginManager pluginManager;
    ExtensionContainer extensions;
    InternalEnvironment internalEnvironment;

    public void configure() {
        pluginManager.withPlugin("java", plugin -> {
            var java = (JavaPluginExtension) extensions.getByName("java");
            java.withSourcesJar();
            if (internalEnvironment.isCi()) {
                java.setSourceCompatibility(JavaLanguageVersion.of(11));
                java.setTargetCompatibility(JavaLanguageVersion.of(11));
            } else {
                java.toolchain(toolchain -> {
                    toolchain.getVendor().set(JvmVendorSpec.AZUL);
                    toolchain.getLanguageVersion().set(JavaLanguageVersion.of(11));
                });
            }
        });
    }
}
