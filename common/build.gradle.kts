plugins {
    `java-library`
    `maven-publish`
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(libs.junit.platform.launcher)
}