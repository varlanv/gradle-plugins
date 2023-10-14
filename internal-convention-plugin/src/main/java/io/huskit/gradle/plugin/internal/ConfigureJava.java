package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmVendorSpec;

@RequiredArgsConstructor
public class ConfigureJava {

    private final PluginManager pluginManager;

    private final ExtensionContainer extensions;

    public void configure() {
        pluginManager.withPlugin("java", plugin -> {
            var java = (JavaPluginExtension) extensions.getByName("java");
            java.withSourcesJar();
            java.toolchain(toolchain -> {
                toolchain.getVendor().set(JvmVendorSpec.AZUL);
                toolchain.getLanguageVersion().set(JavaLanguageVersion.of(11));
            });
        });
    }
}
