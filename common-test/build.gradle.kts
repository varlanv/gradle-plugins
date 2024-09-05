plugins {
    java
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(libs.testcontainers.core)
    implementation(libs.junit.platform.engine)
    implementation(libs.junit.platform.launcher)
    implementation(libs.junit.jupiter.api)
    implementation(libs.apache.commons.lang)
    implementation(libs.apache.commons.io)
    implementation(gradleApi())
    implementation(gradleTestKit())
}

