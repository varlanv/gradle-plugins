plugins {
    java
    `maven-publish`
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(projects.common)
    implementation(libs.slf4j.api)
}
