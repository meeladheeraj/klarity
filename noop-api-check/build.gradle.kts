// A verification-only module. It depends on NEITHER core nor core-noop on its classpath
// (their classes share names and would clash). Instead it loads the two compiled JVM jars
// at test time via separate classloaders and compares their public API surfaces.
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    sourceSets {
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                // present so that classes reflected out of the jars (which reference these)
                // resolve through the parent classloader:
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
            }
        }
    }
}

// Hand the two jars' paths to the test as system properties (built first, lazily resolved).
val coreJar = project(":core").tasks.named("jvmJar")
val coreNoopJar = project(":core-noop").tasks.named("jvmJar")

tasks.named<Test>("jvmTest") {
    dependsOn(coreJar, coreNoopJar)
    doFirst {
        systemProperty("core.jar", coreJar.get().outputs.files.singleFile.absolutePath)
        systemProperty("coreNoop.jar", coreNoopJar.get().outputs.files.singleFile.absolutePath)
    }
}
