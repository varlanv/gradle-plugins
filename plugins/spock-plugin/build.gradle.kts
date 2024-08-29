plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

gradlePlugin {
    plugins {
        create("huskitSpockGradlePlugin") {
            id = "io.huskit.gradle.spock-plugin"
            implementationClass = "io.huskit.gradle.spock.plugin.HuskitSpockPlugin"
        }
    }
}
