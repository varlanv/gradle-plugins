plugins {
    id("java-library")
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(projects.common)
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(libs.bundles.testcontainers)
}
