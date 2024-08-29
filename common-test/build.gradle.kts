plugins {
    groovy
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation(libs.testcontainers.core)
    implementation(libs.groovy.all)
    implementation(libs.spock.core)
    implementation(gradleApi())
    implementation(gradleTestKit())
}

