plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()

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
