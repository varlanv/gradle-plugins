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
            image = "mongo:4.4.8"
            fixedPort = 1
            shouldStartBefore {
                task("test")
            }
        }
    }
}
