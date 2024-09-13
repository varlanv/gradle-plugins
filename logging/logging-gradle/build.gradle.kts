plugins {
    java
    `maven-publish`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.logging.loggingApi)
    implementation(gradleApi())
}
