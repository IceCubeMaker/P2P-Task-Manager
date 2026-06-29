import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                // Navigation
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.7.0-alpha07")
                // Lifecycle
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
                // DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                // SQLDelight
                implementation("app.cash.sqldelight:runtime:2.0.2")
                implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")
                // Koin
                implementation("io.insert-koin:koin-core:3.6.0-Beta4")
                implementation("io.insert-koin:koin-compose:1.2.0-Beta4")
                implementation("io.insert-koin:koin-compose-viewmodel:1.2.0-Beta4")
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("app.cash.sqldelight:android-driver:2.0.2")
                implementation("io.insert-koin:koin-android:3.6.0-Beta4")
                implementation("io.insert-koin:koin-androidx-compose:3.6.0-Beta4")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
                implementation("io.insert-koin:koin-jvm:3.6.0-Beta4")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:web-worker-driver:2.0.2")
            }
        }
    }
}

android {
    namespace = "com.p2ptaskmanager.shared"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.p2ptaskmanager.db")
            srcDirs("src/commonMain/sqldelight")
            generateAsync.set(true)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.p2ptaskmanager.shared.resources"
}
