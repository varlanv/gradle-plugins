plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.common)
    implementation(projects.containers.containersModel)
    implementation(projects.logging.loggingApi)
    implementation(libs.json)
    implementation(libs.apache.commons.lang)
    implementation(libs.apache.commons.io)
    implementation(libs.bundles.testcontainers)
}
