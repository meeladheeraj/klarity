plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvm()
    androidLibrary {     // so the embedded web viewer can run inside an Android app
        namespace = "com.klarity.debugkit.server"
        compileSdk = 35
        minSdk = 24
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation("io.ktor:ktor-server-core:3.0.3")
            implementation("io.ktor:ktor-server-cio:3.0.3")        // the embedded server engine
            implementation("io.ktor:ktor-server-websockets:3.0.3") // WebSocket support
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
        }
    }
}
