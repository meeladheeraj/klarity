// swift-tools-version: 5.9
import PackageDescription

// A thin Swift layer over the DebugKit XCFramework that captures URLSession traffic.
// Build the framework first:
//   ./gradlew :ios-framework:assembleDebugKitReleaseXCFramework
let package = Package(
    name: "DebugKitURLSession",
    platforms: [.iOS(.v16)],
    products: [
        .library(name: "DebugKitURLSession", targets: ["DebugKitURLSession"]),
    ],
    targets: [
        // The Kotlin Multiplatform framework (binary). For distribution you'd host the
        // .xcframework + checksum; this local path points at the Gradle build output.
        .binaryTarget(
            name: "DebugKit",
            path: "../../ios-framework/build/XCFrameworks/release/DebugKit.xcframework"
        ),
        .target(
            name: "DebugKitURLSession",
            dependencies: ["DebugKit"]
        ),
    ]
)
