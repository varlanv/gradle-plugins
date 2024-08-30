plugins {
    groovy
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(gradleApi())
}
