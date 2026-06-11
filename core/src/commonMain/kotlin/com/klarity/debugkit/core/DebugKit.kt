package com.klarity.debugkit.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The public entry point of the toolkit (the start of the plan's `DebugKit.install()` API).
 *
 * It owns the [store] and, on [install], starts a long-lived collector that pipes every
 * event from the [DebugBus] into the store. Producers stay decoupled — they emit to the
 * bus; DebugKit is the one place that knows both the bus and the store.
 *
 * IMPORTANT: call [install] once at app startup, BEFORE any requests. The bus has
 * `replay = 0`, so the store only captures events emitted after its collector subscribes.
 */
object DebugKit {

    val store = EventStore()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var installed = false

    // --- redaction (principle #5) -------------------------------------------
    // Header names whose values must never be logged. Stored lowercased so the
    // match is case-insensitive (HTTP header names are case-insensitive).
    private val redactedHeaders = mutableSetOf<String>()

    /** Register header names to redact, e.g. `DebugKit.redactHeaders("Authorization", "Cookie")`. */
    fun redactHeaders(vararg names: String) {
        redactedHeaders += names.map { it.lowercase() }
    }

    /** True if a header with this name should have its value hidden. */
    fun isHeaderRedacted(name: String): Boolean = name.lowercase() in redactedHeaders

    // --- bounded bodies (principle #4) --------------------------------------
    /**
     * Bodies longer than this (in chars) are truncated before storing.
     * Defaults to [Int.MAX_VALUE] — i.e. keep the ENTIRE body, never truncate.
     * Set a positive limit (e.g. 250_000) if you need to bound memory later.
     */
    var maxBodyChars: Int = Int.MAX_VALUE

    fun install() {
        if (installed) return
        installed = true
        installCrashHandler() // record uncaught exceptions as CrashEvents
        scope.launch {
            DebugBus.events.collect { event ->
                store.record(event)
            }
        }
    }
}
