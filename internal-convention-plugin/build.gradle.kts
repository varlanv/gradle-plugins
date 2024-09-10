plugins {
    `java-gradle-plugin`
}

val isCiBuild = providers.environmentVariable("CI").orNull != null

if (isCiBuild) {
    java {
        version = JavaVersion.VERSION_11
    }
} else {
    java {
        toolchain {
            vendor.set(JvmVendorSpec.AZUL)
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
}

repositories {
    if (!isCiBuild) {
        mavenLocal()
    }
    mavenCentral()
}

dependencies {
    compileOnly(libs.checkerframework.qual)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)
    implementation(libs.junit.platform.launcher)
    annotationProcessor(libs.lombok)
//    testImplementation(libs.groovy.all)
//    testImplementation(libs.spock.core)
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
