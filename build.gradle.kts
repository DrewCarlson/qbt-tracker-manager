plugins {
    kotlin("multiplatform") version "2.1.10"
}

group = "org.drewcarlson"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    linuxX64 { binaries { executable() } }
    linuxArm64 { binaries { executable() } }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.drewcarlson:qbittorrent-client:1.1.0-alpha02")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.5.4")
                implementation("com.github.ajalt.clikt:clikt:5.0.1")
                implementation("io.ktor:ktor-client-cio:3.0.0")
                //implementation("io.ktor:ktor-client-logging:3.0.0")
            }
        }
    }
}
