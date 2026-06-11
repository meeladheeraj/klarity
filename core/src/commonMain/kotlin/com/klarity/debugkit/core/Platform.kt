package com.klarity.debugkit.core

/**
 * The current wall-clock time in milliseconds.
 *
 * `expect` declares a contract that *every* platform must satisfy with an `actual`.
 * Shared code (like the Ktor interceptor) calls this; the compiler guarantees each
 * platform supplies a real implementation. This is the seam between common and native.
 */
expect fun nowMillis(): Long

/**
 * A monotonically increasing event id. Plain shared Kotlin — no platform API needed,
 * so it lives entirely in commonMain.
 *
 * NOTE: not thread-safe (a simple counter). Fine for now; we'll harden it if concurrent
 * producers ever become a real concern.
 */
private var eventCounter = 0L

fun nextEventId(): String = "evt-${eventCounter++}"
