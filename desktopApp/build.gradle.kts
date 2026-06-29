import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
                // MQTT (pure Java Paho client)
                implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
                // ZXing for QR
                implementation("com.google.zxing:core:3.5.3")
                implementation("com.google.zxing:javase:3.5.3")
                // Koin
                implementation("io.insert-koin:koin-core:3.6.0-Beta4")
                // HTTP client for weather API
                implementation("io.ktor:ktor-client-cio:2.3.12")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.p2ptaskmanager.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "P2P Task Manager"
            packageVersion = "1.0.0"
            description = "Peer-to-peer task manager with bullet journal aesthetic"
            vendor = "P2PTaskManager"
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
        }
    }
}
