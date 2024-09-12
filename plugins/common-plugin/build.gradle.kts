plugins {
    alias(libs.plugins.huskitInternalConvention)
}

gradlePlugin {
    plugins {
        create("huskitCommonGradlePlugin") {
            id = "io.huskit.gradle.common-plugin"
            implementationClass = "io.huskit.gradle.common.plugin.HuskitCommonPlugin"
        }
    }
}

dependencies {
    implementation(projects.logging.loggingApi)
}
