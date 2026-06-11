// No-op twin of :interceptor-ktor. Same `DebugKitPlugin` symbol, but it installs no hooks,
// so `client.install(DebugKitPlugin)` compiles and runs in release while capturing nothing.
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("maven-publish")
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.klarity.debugkit.ktor.noop"
        compileSdk = 35
        minSdk = 24
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core-noop"))
            implementation("io.ktor:ktor-client-core:3.0.3") // already in the app; needed for the plugin type
        }
    }
}
