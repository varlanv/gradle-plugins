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

huskitContainers {
    shouldStartBefore {
        task("test")
    }
    mongo {
        image("mongo:4.4.8")
    }
}
