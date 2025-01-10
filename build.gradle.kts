
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "no.nav"

application {
    mainClass.set("no.nav.ApplicationKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "3.0.3"
val kotlinVersion = "2.1.0"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.16")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
