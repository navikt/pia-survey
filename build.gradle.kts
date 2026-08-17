
plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("application")
}

group = "no.nav"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "3.5.2"
val kotlinVersion = "2.4.10"
val kotestVersion = "6.2.3"
val testcontainersVersion = "2.0.5"

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
    implementation("ch.qos.logback:logback-classic:1.6.1")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // -- DB
    implementation("org.postgresql:postgresql:42.7.13")
    implementation("com.zaxxer:HikariCP:7.1.0")
    implementation("org.flywaydb:flyway-database-postgresql:13.2.0")
    implementation("com.github.seratch:kotliquery:1.9.1")

    // -- div
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0-0.6.x-compat")

    // Kafka
    implementation("at.yawk.lz4:lz4-java:1.11.2")
    implementation("org.apache.kafka:kafka-clients:4.3.1") {
        // "Fikser CVE-2025-12183 - lz4-java >1.8.1 har sårbar versjon (transitive dependency fra kafka-clients:4.1.0)"
        exclude("org.lz4", "lz4-java")
    }

    // ----------- test
    testImplementation("org.testcontainers:testcontainers-kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:6.0.0")

    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core") {
            version { require("2.22.1") }
            because("versjoner < 2.22.1 har sårbarhet. inkludert i ktor-server-auth:3.5.0")
        }
        implementation("tools.jackson.core:jackson-core") {
            version { require("3.2.1") }
            because("versjoner <= 3.2.0 har sårbarhet. inkludert i logstash-logback-encoder:9.0")
        }
        implementation("io.netty:netty-codec-http2") {
            version {
                require("4.2.16.Final")
            }
            because(
                "versjoner < 4.2.16.Final har sårbarhet. inkludert i ktor-server-netty-jvm:3.4.2",
            )
        }
    }
}

tasks {
    test {
        dependsOn(installDist)
    }
}
