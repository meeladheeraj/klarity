// The no-op twin of :core. Same public API, empty bodies. An app uses
// releaseImplementation(:core-noop) so the real capture engine never ships to production.
// Note: it deliberately depends on NOTHING heavy — no serialization, no Ktor — only
// kotlinx-coroutines for the Flow types in the public signatures.
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("maven-publish")
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.klarity.debugkit.core.noop"
        compileSdk = 35
        minSdk = 24
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
    }
}
