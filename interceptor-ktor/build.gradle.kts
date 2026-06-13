plugins {
    kotlin("multiplatform") // version comes from the root build.gradle.kts
    id("com.android.kotlin.multiplatform.library")
    id("com.vanniktech.maven.publish")
}

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.klarity.debugkit.ktor"
        compileSdk = 35
        minSdk = 24
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))             // our event model + bus
            implementation("io.ktor:ktor-client-core:3.0.3") // the HttpClient + plugin API
        }
    }
}
