pluginManagement {
    repositories {
        if (providers.environmentVariable("CI").getOrNull() != null) {
            mavenLocal()
        }
        gradlePluginPortal()
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention").version(providers.gradleProperty("foojayToolchainPluginVersion").get())
    }
    includeBuild("internal-convention-plugin")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "huskit"

val isCi = providers.environmentVariable("CI").getOrNull()?.let { it != "false" } ?: false

buildCache {
    local {
        isEnabled = !isCi
        isPush = !isCi
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("internal-convention-plugin")

include(
        "logging:logging-api",
        "logging:logging-gradle",
        "common",
        "common-test",
        "functional-test",
        "plugins:aspectj-plugin",
        "plugins:spock-plugin",
        "plugins:properties-plugin",
        "plugins:common-plugin",
        "plugins:containers-plugin:containers-gradle-plugin",
        "plugins:containers-plugin:containers-model",
        "plugins:containers-plugin:containers-core",
        "plugins:containers-plugin:containers-testcontainers"
)
