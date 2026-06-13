import com.vanniktech.maven.publish.MavenPublishBaseExtension

// Root build file. We declare shared plugins here *once* with `apply false` — this puts
// them on the build classpath at a single version without applying them to the root.
// Each module then applies them (no version) and they all share these.
plugins {
    kotlin("multiplatform") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false // JSON for the web viewer
    id("com.android.kotlin.multiplatform.library") version "8.7.3" apply false // modern KMP Android target (non-Compose modules)
    id("com.android.library") version "8.7.3" apply false // used by :ui — Compose MP's Android support needs this one
    id("org.jetbrains.compose") version "1.11.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false // Compose compiler, matches Kotlin
    id("com.vanniktech.maven.publish") version "0.36.0" apply false // publishing to Maven Central Portal
}

subprojects {
    group = "io.github.meeladheeraj" // verified Maven Central namespace (Kotlin package stays com.klarity.debugkit)
    version = "0.1.0"

    // Configured once for any module that applies the vanniktech publish plugin (the 4 library
    // modules). It handles POM, sources + javadoc jars, and the Central Portal upload.
    plugins.withId("com.vanniktech.maven.publish") {
        // Two signing paths: in-memory (CI-friendly) OR the local `gpg` binary (set
        // -Psigning.gnupg.keyName=...). gpg-cmd is the robust path for modern GnuPG whose
        // secret-key format Gradle's bundled BouncyCastle can't always read.
        val gpgKeyName = providers.gradleProperty("signing.gnupg.keyName")
        val hasInMemoryKey = providers.environmentVariable("ORG_GRADLE_PROJECT_signingInMemoryKey").isPresent ||
            providers.gradleProperty("signingInMemoryKey").isPresent

        if (gpgKeyName.isPresent) {
            // vanniktech applies the `signing` plugin itself (via signAllPublications), so wait
            // for it before configuring the signatory.
            plugins.withId("signing") {
                extensions.configure<org.gradle.plugins.signing.SigningExtension> { useGpgCmd() }
            }
        }

        configure<MavenPublishBaseExtension> {
            publishToMavenCentral() // the new Central Portal (no-arg default)

            // Sign only when a key is configured, so `publishToMavenLocal` works keyless locally.
            if (hasInMemoryKey || gpgKeyName.isPresent) {
                signAllPublications()
            }

            pom {
                name.set("DebugKit (${project.name})")
                description.set(
                    "Kotlin Multiplatform debug toolkit — Ktor HTTP capture, a Compose overlay, and a live web viewer."
                )
                url.set("https://github.com/meeladheeraj/klarity")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("meela")
                        name.set("Meela")
                        url.set("https://github.com/meeladheeraj")
                    }
                }
                scm {
                    url.set("https://github.com/meeladheeraj/klarity")
                    connection.set("scm:git:https://github.com/meeladheeraj/klarity.git")
                    developerConnection.set("scm:git:ssh://git@github.com/meeladheeraj/klarity.git")
                }
            }
        }
    }
}
