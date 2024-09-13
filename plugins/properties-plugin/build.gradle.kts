plugins {
    alias(libs.plugins.huskitInternalConvention)
}

gradlePlugin {
    plugins {
        create("huskitPropertiesGradlePlugin") {
            id = "io.huskit.gradle.properties-plugin"
            implementationClass = "io.huskit.gradle.properties.plugin.HuskitPropertiesPlugin"
        }
    }
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(projects.logging.loggingGradle)
    implementation(projects.plugins.commonPlugin)
}
