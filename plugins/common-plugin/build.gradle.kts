plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
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
