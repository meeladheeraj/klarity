// Compose 1.11 deprecated the `compose.runtime/foundation/material3` shortcut accessors,
// but the suggested alternative (hardcoded artifact coordinates) is MORE fragile here:
// material3 tracks a different version line than the Compose plugin. The managed accessors
// resolve the correct versions for us, so we keep them and suppress the (harmless) warning.
@file:Suppress("DEPRECATION")

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    // Compose Multiplatform's Android support integrates with the classic library plugin,
    // not the new com.android.kotlin.multiplatform.library (incompatible at AGP 8.7.3).
    id("com.android.library")
}

kotlin {
    jvm("desktop")       // a JVM target we can run as a native desktop window from the terminal
    androidTarget()      // the overlay as an Android Compose component
    // Compose MP's androidx deps no longer ship iosX64 (Intel sim); arm64 device + Apple-Silicon sim only.
    iosArm64()           // the overlay rendered by Compose Multiplatform on iOS (device)
    iosSimulatorArm64()  // Apple-Silicon simulator

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(compose.runtime)     // @Composable, state
            implementation(compose.foundation)  // layout, LazyColumn, clickable
            implementation(compose.material3)   // Material 3 widgets + theme
            implementation(compose.ui)          // ComposeUIViewController (iOS), core ui
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)      // desktop window runtime (Skia)
                implementation(project(":interceptor-ktor"))   // the DebugKit Ktor plugin
                implementation(project(":server"))             // embedded web viewer
                implementation("io.ktor:ktor-client-core:3.0.3")
                implementation("io.ktor:ktor-client-mock:3.0.3") // fake traffic source
            }
        }
    }
}

android {
    namespace = "com.klarity.debugkit.ui"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}

compose.desktop {
    application {
        mainClass = "com.klarity.debugkit.ui.MainKt" // ./gradlew :ui:run
    }
}
