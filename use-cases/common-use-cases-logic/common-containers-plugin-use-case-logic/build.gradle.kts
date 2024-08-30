plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.mongodb:mongodb-driver-sync:4.10.2")
    api("org.junit.jupiter:junit-jupiter-api:5.9.2")
    api("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}
