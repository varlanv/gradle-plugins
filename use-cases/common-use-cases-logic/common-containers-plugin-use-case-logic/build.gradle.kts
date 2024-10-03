plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.mongodb:mongodb-driver-sync:4.11.4")
    api("org.junit.jupiter:junit-jupiter-api:5.9.2")
    api("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    api("org.slf4j:slf4j-api:2.0.16")
}
