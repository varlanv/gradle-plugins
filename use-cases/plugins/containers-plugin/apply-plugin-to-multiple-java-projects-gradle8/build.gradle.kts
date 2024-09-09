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
            shouldStartBefore {
                task("test")
            }
            mongo {
                image("mongo:4.4.8")
                reuse {
                    newDatabaseForEachTask(true)
                    reuseBetweenBuilds(true)
                    cleanup {
                        after(60, "seconds")
                    }
                }
            }
        }
    }
}
