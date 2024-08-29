import io.huskit.gradle.containers.plugin.HuskitContainersPlugin
import io.huskit.gradle.containers.plugin.api.ContainersExtension

plugins {
    id("io.huskit.gradle.containers-plugin").apply(false)
}

subprojects {
    plugins.withId("java") {
        repositories {
            mavenCentral()
        }
        tasks.withType<Test> {
            useJUnitPlatform()
            outputs.upToDateWhen { false }
        }

        project.dependencies {
            add("implementation", "org.mongodb:mongodb-driver-sync:4.10.2")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.9.2")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.9.2")
        }
    }

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
