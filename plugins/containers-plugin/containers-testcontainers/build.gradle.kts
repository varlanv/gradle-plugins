plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
    id("java-library")
    id("maven-publish")
    id("groovy")
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.mongodb)
    implementation(libs.testcontainers.postgresql)
    implementation(libs.testcontainers.kafka)
    implementation(libs.testcontainers.rabbitmq)
}
