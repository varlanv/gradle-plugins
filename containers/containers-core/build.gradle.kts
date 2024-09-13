plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
}

java {
    modularity.inferModulePath = true
}

dependencies {
    implementation(projects.common)
    implementation(projects.logging.loggingApi)
    implementation(projects.containers.containersModel)
    implementation(projects.containers.containersTestcontainers)
    testImplementation(libs.bundles.testcontainers)
}
