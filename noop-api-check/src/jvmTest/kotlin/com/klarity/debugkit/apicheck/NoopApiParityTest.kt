package com.klarity.debugkit.apicheck

import java.io.File
import java.lang.reflect.Member
import java.lang.reflect.Modifier
import java.net.URLClassLoader
import java.util.jar.JarFile
import kotlin.test.Test
import kotlin.test.fail

/**
 * Fails the build if `core` exposes any public API symbol that `core-noop` does not —
 * i.e. if the no-op twin has drifted out of sync and a consumer's release build would break.
 *
 * The two modules share the package `com.klarity.debugkit.core` with identical class names,
 * so they can't both be on one classpath. We load each from its own jar via a separate
 * URLClassLoader and compare public-member *signatures*, ignoring serialization-generated
 * members (Companion / $serializer / synthetics) that exist only in the real `core`.
 */
class NoopApiParityTest {

    private val pkg = "com.klarity.debugkit.core"
    private val pkgPath = pkg.replace('.', '/')

    // The public named types the real core exposes (top-level functions handled separately).
    private val apiTypes = listOf(
        "DebugKit", "EventStore", "DebugBus",
        "DebugEvent", "LogEvent", "HttpEvent", "TruncatedBody",
    )

    @Test
    fun coreNoopMirrorsCorePublicApi() {
        val coreJar = File(requireNotNull(System.getProperty("core.jar")) { "core.jar not set" })
        val noopJar = File(requireNotNull(System.getProperty("coreNoop.jar")) { "coreNoop.jar not set" })

        val coreLoader = URLClassLoader(arrayOf(coreJar.toURI().toURL()), javaClass.classLoader)
        val noopLoader = URLClassLoader(arrayOf(noopJar.toURI().toURL()), javaClass.classLoader)

        val missing = mutableListOf<String>()

        // 1) Named types: every public member of core's type must exist in core-noop's type.
        for (type in apiTypes) {
            val coreClass = coreLoader.loadClass("$pkg.$type")
            val noopClass = try {
                noopLoader.loadClass("$pkg.$type")
            } catch (e: ClassNotFoundException) {
                missing += "missing class: $type"
                continue
            }
            val noopMembers = publicMembers(noopClass)
            (publicMembers(coreClass) - noopMembers).forEach { missing += "$type#$it" }
        }

        // 2) Top-level functions/consts live in *Kt classes whose names differ between modules,
        //    so aggregate them across all *Kt classes and compare the flattened sets.
        val coreTop = topLevelMembers(coreJar, coreLoader)
        val noopTop = topLevelMembers(noopJar, noopLoader)
        (coreTop - noopTop).forEach { missing += "<top-level> $it" }

        if (missing.isNotEmpty()) {
            fail(
                "core-noop is missing ${missing.size} public symbol(s) present in core " +
                    "(update core-noop to restore API symmetry):\n" +
                    missing.sorted().joinToString("\n") { "  - $it" }
            )
        }
    }

    private fun publicMembers(c: Class<*>): Set<String> {
        val out = mutableSetOf<String>()
        c.declaredMethods
            .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic && !isNoise(it) }
            .forEach { out += "${it.name}(${it.parameterTypes.joinToString(",") { p -> p.name }}):${it.returnType.name}" }
        c.declaredFields
            .filter { Modifier.isPublic(it.modifiers) && !it.isSynthetic && !isNoise(it) }
            .forEach { out += "field ${it.name}:${it.type.name}" }
        return out
    }

    private fun topLevelMembers(jar: File, loader: ClassLoader): Set<String> {
        val out = mutableSetOf<String>()
        JarFile(jar).use { jf ->
            jf.entries().asSequence()
                .map { it.name }
                .filter { it.startsWith("$pkgPath/") && it.endsWith("Kt.class") }
                .map { it.removeSuffix(".class").replace('/', '.') }
                .forEach { className ->
                    val c = loader.loadClass(className)
                    c.declaredMethods
                        .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) && !it.isSynthetic && !isNoise(it) }
                        .forEach { out += "${it.name}(${it.parameterTypes.joinToString(",") { p -> p.name }}):${it.returnType.name}" }
                    c.declaredFields
                        .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) && !it.isSynthetic && !isNoise(it) }
                        .forEach { out += "field ${it.name}:${it.type.name}" }
                }
        }
        return out
    }

    /** Serialization-generated members exist only in the real core; they aren't part of the contract. */
    private fun isNoise(m: Member): Boolean {
        val n = m.name
        return n == "Companion" || n == "serializer" || n.contains("$")
    }
}
