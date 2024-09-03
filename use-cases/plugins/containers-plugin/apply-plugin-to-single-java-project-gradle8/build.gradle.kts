import io.huskit.gradle.containers.plugin.HuskitContainersPlugin
import io.huskit.gradle.containers.plugin.api.ContainersExtension

plugins {
    java
    id("io.huskit.gradle.containers-plugin")
}

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

    dependencies {
        implementation("plugin-usecases:usecases")
    }
}

plugins.withType<HuskitContainersPlugin> {
    extensions.configure(ContainersExtension::class.java) {
        mongo {
            image.set("mongo:4.4.8") // can use `image = ".."` in later gradle versions
            fixedPort.set(1) // can use `fixedPort = 1` in later gradle versions
            shouldStartBefore {
                task("test")
            }
        }
    }
}
