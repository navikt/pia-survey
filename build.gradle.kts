
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "no.nav"

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val kotlinVersion = "2.1.0"
val kotestVersion = "5.9.1"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

tasks {
    shadowJar {
        manifest {
            attributes("Main-Class" to "no.nav.pia.survey.ApplicationKt")
        }
    }
    test {
        dependsOn(shadowJar)
    }
}
