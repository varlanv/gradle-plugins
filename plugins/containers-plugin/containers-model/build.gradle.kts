plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
    id("java-library")
    id("maven-publish")
}

dependencies {
    implementation(projects.logging.loggingApi)
}
