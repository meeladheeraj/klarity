package com.klarity.debugkit.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The single thing every part of the toolkit speaks: a debug event.
 *
 * Producers (Ktor plugin, logger, crash hook) create these; consumers
 * (Compose overlay, web viewer) render them. Neither side knows the other exists.
 *
 * `sealed` means the set of subtypes is closed and known at compile time, so a
 * `when (event)` over them can be exhaustive — the compiler forces us to handle
 * every kind of event.
 *
 * `@Serializable` on the sealed interface enables *polymorphic* JSON: each subtype is
 * tagged with a `"type"` discriminator (its `@SerialName`), so the web viewer's JS can
 * switch on `ev.type`.
 */
@Serializable
sealed interface DebugEvent {
    val id: String
    val timestamp: Long
}

/** A plain log line. The simplest event — our starting point. */
@Serializable
@SerialName("log")
data class LogEvent(
    override val id: String,
    override val timestamp: Long,
    val message: String,
    val level: String = "INFO",
) : DebugEvent

/** A captured HTTP request/response. Emitted by the Ktor interceptor. */
@Serializable
@SerialName("http")
data class HttpEvent(
    override val id: String,
    override val timestamp: Long,
    val method: String,
    val url: String,
    val statusCode: Int?,
    val durationMs: Long,
    val requestHeaders: Map<String, String> = emptyMap(),
    val responseHeaders: Map<String, String> = emptyMap(),
    val responseBody: String? = null,
    val responseBodyTruncated: Boolean = false,
) : DebugEvent

/** An uncaught exception captured by the crash hook. */
@Serializable
@SerialName("crash")
data class CrashEvent(
    override val id: String,
    override val timestamp: Long,
    val errorType: String, // NB: not `type` — that's the polymorphic JSON discriminator
    val message: String?,
    val stackTrace: String,
) : DebugEvent
