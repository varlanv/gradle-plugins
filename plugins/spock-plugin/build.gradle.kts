plugins {
    alias(libs.plugins.huskitInternalConvention)
}

gradlePlugin {
    plugins {
        create("huskitSpockGradlePlugin") {
            id = "io.huskit.gradle.spock-plugin"
            implementationClass = "io.huskit.gradle.spock.plugin.HuskitSpockPlugin"
        }
    }
}
