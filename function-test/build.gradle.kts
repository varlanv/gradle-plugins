plugins {
    id("java-gradle-plugin")
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

huskitConvention {
    integrationTestName = "functionalTest"
}

dependencies {
    implementation(projects.commonTest)
    implementation(projects.plugins.propertiesPlugin)
    implementation(projects.plugins.commonPlugin)
    implementation(projects.plugins.containersPlugin.containersGradlePlugin)
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(libs.apache.commons)
    implementation(libs.testcontainers.core)
    implementation(libs.testcontainers.mongodb)
    testImplementation(gradleTestKit())
}
