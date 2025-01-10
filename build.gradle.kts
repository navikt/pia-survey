
plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "no.nav"
version = "0.0.1"

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
