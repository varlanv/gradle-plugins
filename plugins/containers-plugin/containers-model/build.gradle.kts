plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
    id("java-library")
    id("groovy")
    id("maven-publish")
}

dependencies {
    implementation(projects.logging.loggingApi)
}
