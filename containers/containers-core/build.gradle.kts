plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.common)
    implementation(projects.containers.containersHuskit)
    testImplementation(libs.bundles.testcontainers)
}
