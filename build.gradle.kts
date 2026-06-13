import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

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
}

// Shared Maven coordinates for every publishable module. Only modules that apply
// `maven-publish` actually produce artifacts; the rest just inherit these harmlessly.
subprojects {
    group = "com.klarity.debugkit"
    version = "0.1.0"

    // Configured once for ANY module that applies maven-publish (the 4 library modules).
    plugins.withId("maven-publish") {

        // Maven Central requires a javadoc jar on each publication (an empty one satisfies it).
        val javadocJar = tasks.register<Jar>("javadocJar") {
            archiveClassifier.set("javadoc")
        }

        extensions.configure<PublishingExtension> {
            publications.withType<MavenPublication>().configureEach {
                artifact(javadocJar)
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

        // Sign publications ONLY when a key is configured, so `publishToMavenLocal` still
        // works locally without one; CI provides SIGNING_KEY / SIGNING_PASSWORD to sign.
        apply(plugin = "signing")
        extensions.configure<SigningExtension> {
            val signingKey = providers.environmentVariable("SIGNING_KEY").orNull
            val signingPassword = providers.environmentVariable("SIGNING_PASSWORD").orNull
            if (signingKey != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
}
