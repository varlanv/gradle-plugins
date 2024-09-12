plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.huskitInternalConvention)
}

dependencies {
    implementation(libs.junit.platform.launcher)
}