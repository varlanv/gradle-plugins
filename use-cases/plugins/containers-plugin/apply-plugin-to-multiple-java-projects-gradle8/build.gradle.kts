import io.huskit.gradle.containers.plugin.HuskitContainersPlugin
import io.huskit.gradle.containers.plugin.api.ContainersExtension

plugins {
    id("io.huskit.gradle.containers-plugin").apply(false)
}

subprojects {
    project.apply(mapOf("plugin" to "java"))
    project.apply(mapOf("plugin" to "io.huskit.gradle.containers-plugin"))

    plugins.withId("java") {
        repositories {
            mavenCentral()
        }
        tasks.withType<Test> {
            useJUnitPlatform()
            testLogging {
                showStandardStreams = true
            }
            outputs.upToDateWhen { false }
        }

        project.dependencies {
            add("implementation", "plugin-usecases:usecases")
        }
    }

    plugins.withType<HuskitContainersPlugin> {
        extensions.configure(ContainersExtension::class.java) {
            mongo {
                image.set("mongo:4.4.8") // can use `image = "..."` in later gradle versions
                fixedPort.set(1) // can use `fixedPort = 1` in later gradle versions
                shouldStartBefore {
                    task("test")
                }
                reuse {
                    newDatabaseForEachTask.set(true) // can use `newDatabaseForEachTask = true` in later gradle versions
                    reuseBetweenBuilds.set(true) // can use `reuseBetweenBuilds = true` in later gradle versions
                }
            }
        }
    }
}
