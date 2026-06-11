// Compose 1.11 deprecated the `compose.runtime/foundation/material3` shortcut accessors,
// but the suggested alternative (hardcoded artifact coordinates) is MORE fragile here:
// material3 tracks a different version line than the Compose plugin. The managed accessors
// resolve the correct versions for us, so we keep them and suppress the (harmless) warning.
@file:Suppress("DEPRECATION")

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop") // a JVM target we can run as a native desktop window from the terminal

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(compose.runtime)     // @Composable, state
            implementation(compose.foundation)  // layout, LazyColumn, clickable
            implementation(compose.material3)   // Material 3 widgets + theme
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

compose.desktop {
    application {
        mainClass = "com.klarity.debugkit.ui.MainKt" // ./gradlew :ui:run
    }
}
