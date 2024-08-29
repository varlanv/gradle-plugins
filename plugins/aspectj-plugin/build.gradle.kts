plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

gradlePlugin {
    plugins {
        create("huskitAspectJGradlePlugin") {
            id = "io.huskit.gradle.aspectj-plugin"
            implementationClass = "io.huskit.gradle.aspectj.plugin.HuskitAspectJPlugin"
        }
    }
}
