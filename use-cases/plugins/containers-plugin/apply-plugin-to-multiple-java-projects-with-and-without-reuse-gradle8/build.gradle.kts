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
        tasks.withType < Test > {
            useJUnitPlatform()
            outputs.upToDateWhen { false }
        }

        project.dependencies {
            add("implementation", "plugin-usecases:usecases")
        }
    }

    if (project.name == "project1"|| project.name == "project3") {
        plugins.withType<HuskitContainersPlugin> {
            extensions.configure(ContainersExtension::class.java) {
                mongo {
                    image = "mongo:4.4.8"
                    fixedPort = 1
                    shouldStartBefore {
                        task("test")
                    }
                    reuse {
                        newDatabaseForEachTask = true
                        reuseBetweenBuilds = true
                    }
                }
            }
        }
    } else {
        plugins.withType<HuskitContainersPlugin> {
            extensions.configure(ContainersExtension::class.java) {
                mongo {
                    image = "mongo:4.4.8"
                    fixedPort = 1
                    shouldStartBefore {
                        task("test")
                    }
                }
            }
        }
    }
}
