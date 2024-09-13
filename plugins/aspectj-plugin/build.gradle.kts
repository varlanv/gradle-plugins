plugins {
    alias(libs.plugins.huskitInternalConvention)
}

gradlePlugin {
    plugins {
        create("huskitAspectJGradlePlugin") {
            id = "io.huskit.gradle.aspectj-plugin"
            implementationClass = "io.huskit.gradle.aspectj.plugin.HuskitAspectJPlugin"
        }
    }
}
