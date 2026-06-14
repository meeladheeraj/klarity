plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvm()
    androidLibrary {     // the modern KMP Android target: declares + configures in one place
        namespace = "com.klarity.debugkit.core"
        compileSdk = 35
        minSdk = 24
    }
    iosX64()             // intel simulator
    iosArm64()           // physical device
    iosSimulatorArm64()  // apple-silicon simulator

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            implementation("com.squareup.okio:okio:3.17.0") // multiplatform file I/O for persistence
        }
        commonTest.dependencies {
            implementation(kotlin("test")) // JUnit on JVM, native runner on iOS — one source, every target
            implementation("com.squareup.okio:okio-fakefilesystem:3.17.0")
        }
    }
}
