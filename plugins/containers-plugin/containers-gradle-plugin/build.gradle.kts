plugins {
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

gradlePlugin {
    plugins {
        create("huskitContainersGradlePlugin") {
            id = "io.huskit.gradle.containers-plugin"
            implementationClass = "io.huskit.gradle.containers.plugin.HuskitContainersPlugin"
        }
    }
}

dependencies {
    implementation(projects.plugins.containersPlugin.containersModel)
    implementation(projects.plugins.containersPlugin.containersTestcontainers)
    implementation(projects.plugins.commonPlugin)
}
