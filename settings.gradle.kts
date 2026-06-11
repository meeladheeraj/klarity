rootProject.name = "klarity-debugkit"

pluginManagement {
    repositories {
        google()           // the Android Gradle Plugin is hosted here
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()        // Compose Multiplatform pulls some artifacts from here
        mavenCentral()
    }
}

include(":core")
include(":interceptor-ktor")
include(":ui")
include(":server")
include(":core-noop")
include(":interceptor-ktor-noop")
include(":noop-api-check")
include(":ios-framework")
