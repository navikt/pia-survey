
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "no.nav"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "3.0.3"
val kotlinVersion = "2.1.0"
val kotestVersion = "5.9.1"
val testcontainersVersion = "1.20.4"

dependencies {
    // -- ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")

    // -- logs
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // -- DB
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.1.1")
    implementation("com.github.seratch:kotliquery:1.9.1")

    // -- div
    implementation("com.github.navikt:ia-felles:1.9.0")
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // ----------- test
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:2.1.10")
}

tasks {
    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes("Main-Class" to "no.nav.pia.survey.ApplicationKt")
        }
    }
    test {
        dependsOn(shadowJar)
    }
}
