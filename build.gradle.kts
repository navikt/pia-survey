
plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "no.nav"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "3.1.1"
val kotlinVersion = "2.1.10"
val kotestVersion = "5.9.1"
val testcontainersVersion = "1.20.6"

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
    implementation("ch.qos.logback:logback-classic:1.5.17")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // -- DB
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-database-postgresql:11.3.4")
    implementation("com.github.seratch:kotliquery:1.9.1")

    // -- div
    implementation("com.github.navikt:ia-felles:1.10.2")
    implementation("org.apache.kafka:kafka-clients:3.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // ----------- test
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:2.1.10")

    constraints {
        implementation("net.minidev:json-smart") {
            version {
                require("2.5.2")
            }
            because(
                "versjoner < 2.5.2 har diverse sårbarheter",
            )
        }
        implementation("io.netty:netty-codec-http2") {
            version {
                require("4.1.119.Final")
            }
            because(
                "Versjoner <4.1.117 er sårbare. Inkludert i ktor 3.1.0",
            )
        }
    }
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
