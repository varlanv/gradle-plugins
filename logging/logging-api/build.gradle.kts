plugins {
    groovy
    id("io.huskit.gradle.internal-gradle-convention-plugin")
}

dependencies {
    implementation("org.slf4j:slf4j-api:${project.properties["slf4jVersion"]}")
}
