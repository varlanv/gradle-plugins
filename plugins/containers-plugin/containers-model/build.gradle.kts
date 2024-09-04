plugins {
    id("java-library")
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(projects.common)
    implementation(projects.logging.loggingApi)
}
