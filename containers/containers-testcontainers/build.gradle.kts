plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(projects.common)
    implementation(projects.containers.containersModel)
    implementation(libs.bundles.testcontainers)
}
