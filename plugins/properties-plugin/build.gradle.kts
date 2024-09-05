plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
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
