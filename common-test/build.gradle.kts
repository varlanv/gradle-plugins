plugins {
    java
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(libs.testcontainers.core)
    implementation(libs.junit.jupiter.api)
    implementation(gradleApi())
    implementation(gradleTestKit())
}

