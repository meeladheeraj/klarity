import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

// Umbrella module that packages the toolkit's public API into a DebugKit.xcframework
// for Swift / Xcode / SPM consumers (who can't use Gradle Module Metadata).
plugins {
    kotlin("multiplatform")
}

kotlin {
    // One XCFramework bundling every iOS arch slice (device + both simulators).
    val xcframework = XCFramework("DebugKit")

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "DebugKit"
            isStatic = true
            export(project(":core")) // expose DebugKit / EventStore / DebugEvent to Swift
            xcframework.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core")) // `api` (not implementation) is required for `export` to work
        }
    }
}
