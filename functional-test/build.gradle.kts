plugins {
    `java-gradle-plugin`
    alias(libs.plugins.huskitInternalConvention)
}

huskitConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.commonTest)
    implementation(projects.logging.loggingApi)
    implementation(projects.logging.loggingGradle)
    implementation(projects.plugins.commonPlugin)
    implementation(projects.plugins.containersPlugin.containersGradlePlugin)
    implementation(projects.containers.containersModel)
    implementation(projects.containers.containersTestcontainers)
    testImplementation(libs.apache.commons.lang)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(gradleTestKit())
}
