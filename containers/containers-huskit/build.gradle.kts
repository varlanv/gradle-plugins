import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `maven-publish`
    `java-library`
    alias(libs.plugins.huskitInternalConvention)
    alias(libs.plugins.shadow)
}

tasks.named<ShadowJar>("shadowJar", {
    dependencies {
        exclude(dependency("org.slf4j:slf4j-api"))
    }
    relocate("io.huskit.common", "io.huskit.shadow.common")
    relocate("io.huskit.log", "io.huskit.shadow.log")
    relocate("org.json", "io.huskit.shadow.json")
    minimize()
})

dependencies {
    implementation(projects.common)
    implementation(projects.containers.containersModel)
    implementation(projects.logging.loggingApi)
    implementation(libs.json)
    implementation(libs.github.docker.java)
    implementation(libs.github.docker.transport)
//    implementation(libs.mongoDriver)
//    implementation(libs.apache.commons.lang)
//    implementation(libs.apache.commons.io)
//    implementation(libs.bundles.testcontainers)
}
