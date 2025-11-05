
plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("application")
}

group = "no.nav"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "3.3.2"
val kotlinVersion = "2.2.21"
val kotestVersion = "6.0.4"
val testcontainersVersion = "2.0.1"

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
    implementation("ch.qos.logback:logback-classic:1.5.20")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")

    // -- DB
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.15.0")
    implementation("com.github.seratch:kotliquery:1.9.1")

    // -- div
    implementation("org.apache.kafka:kafka-clients:4.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")

    // ----------- test
    testImplementation("org.testcontainers:testcontainers-kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:3.0.1")
}

tasks {
    test {
        dependsOn(installDist)
    }
}
