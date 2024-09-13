plugins {
    java
    `maven-publish`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(projects.common)
    implementation(libs.slf4j.api)
}
