plugins {
    id("groovy")
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
    implementation(libs.groovy.all)
    implementation(libs.spock.core)
    testImplementation(gradleTestKit())
}
