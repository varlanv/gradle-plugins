plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.common)
    implementation(projects.logging.loggingApi)
    implementation(projects.containers.containersModel)
    implementation(projects.containers.containersTestcontainers)
    testImplementation(libs.bundles.testcontainers)
}
