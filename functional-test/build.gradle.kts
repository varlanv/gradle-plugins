plugins {
    `java-gradle-plugin`
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

huskitConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.commonTest)
    implementation(projects.logging.loggingApi)
    implementation(projects.logging.loggingGradle)
    implementation(projects.plugins.propertiesPlugin)
    implementation(projects.plugins.commonPlugin)
    implementation(projects.plugins.containersPlugin.containersGradlePlugin)
    implementation(projects.containers.containersModel)
    implementation(projects.containers.containersTestcontainers)
    testImplementation(libs.apache.commons.lang)
    testImplementation(libs.bundles.testcontainers)
    testImplementation(gradleTestKit())
}
