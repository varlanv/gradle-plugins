plugins {
    `maven-publish`
    `java-library`
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(projects.common)
    implementation(projects.logging.loggingApi)
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(projects.plugins.containersPlugin.containersTestcontainers)
    testImplementation(libs.bundles.testcontainers)
}
