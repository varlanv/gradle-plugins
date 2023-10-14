package io.huskit.gradle.plugin;

import org.gradle.api.provider.Property;

public interface HuskitInternalConventionExtension {

    String EXTENSION_NAME = "huskitConvention";

    Property<String> getIntegrationTestName();
}
