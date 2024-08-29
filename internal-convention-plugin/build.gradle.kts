plugins {
    groovy
    `java-gradle-plugin`
}

java {
    toolchain {
        vendor.set(JvmVendorSpec.AZUL)
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val isCiBuild = providers.environmentVariable("CI").orNull != null

repositories {
    if (!isCiBuild) {
        mavenLocal()
    }
    mavenCentral()
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.groovy.all)
    testImplementation(libs.spock.core)
}

gradlePlugin {
    plugins {
        create("huskitInternalGradleConventionPlugin") {
            id = "io.huskit.gradle.internal-gradle-convention-plugin"
            implementationClass = "io.huskit.gradle.plugin.InternalConventionPlugin"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
