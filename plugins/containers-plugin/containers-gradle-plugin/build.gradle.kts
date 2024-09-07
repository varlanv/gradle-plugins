plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
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
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(projects.plugins.containersPlugin.containersCore)
    implementation(projects.plugins.containersPlugin.containersTestcontainers)
    implementation(projects.plugins.commonPlugin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.bundles.testcontainers)
}
