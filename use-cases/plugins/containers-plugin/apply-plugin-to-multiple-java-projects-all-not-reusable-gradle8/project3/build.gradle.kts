plugins {
    java
    id("io.huskit.gradle.containers-plugin")
}

dependencies {
    implementation(project(":mongo-logic"))
}
