
plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "no.nav"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktorVersion = "3.2.3"
val kotlinVersion = "2.2.0"
val kotestVersion = "5.9.1"
val testcontainersVersion = "1.21.3"

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
    implementation("ch.qos.logback:logback-classic:1.5.18")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    // -- DB
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.zaxxer:HikariCP:7.0.0")
    implementation("org.flywaydb:flyway-database-postgresql:11.10.5")
    implementation("com.github.seratch:kotliquery:1.9.1")

    // -- div
    implementation("org.apache.kafka:kafka-clients:4.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // ----------- test
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-cio:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:2.2.1")

    constraints {
        implementation("io.netty:netty-codec-http2") {
            version {
                require("4.2.4.Final")
            }
            because(
                "ktor-server-netty har sårbar versjon",
            )
        }

        testImplementation("org.apache.commons:commons-compress") {
            version {
                require("1.28.0")
            }
            because("testcontainers har sårbar versjon")
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
