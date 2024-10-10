plugins {
    alias(libs.plugins.huskitInternalConvention)
}

gradlePlugin {
    plugins {
        create("huskitContainersGradlePlugin") {
            id = "io.huskit.gradle.containers-plugin"
            implementationClass = "io.huskit.gradle.containers.plugin.HuskitContainersPlugin"
        }
    }
}

dependencies {
    implementation(projects.common)
    implementation(projects.logging.loggingGradle)
    implementation(projects.logging.loggingApi)
    implementation(projects.containers.containersCore)
    implementation(projects.containers.containersHuskit)
    implementation(projects.plugins.commonPlugin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.bundles.testcontainers)
}
