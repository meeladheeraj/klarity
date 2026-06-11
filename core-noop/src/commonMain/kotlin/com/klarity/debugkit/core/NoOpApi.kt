package com.klarity.debugkit.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * NO-OP build of :core. Every public symbol of the real `core` is mirrored here with the
 * SAME signature but an empty/inert body. An app that does
 *     releaseImplementation(":core-noop")
 * compiles identically against this, but captures nothing and ships no capture engine.
 *
 * Keep this file in lock-step with the real core's public API — that's the cost of the
 * pattern. (Notice: no @Serializable, no coroutine scopes, no work — just empty shells.)
 */

// --- event model (data only; identical shapes, minus the serialization annotations) ---
sealed interface DebugEvent {
    val id: String
    val timestamp: Long
}

data class LogEvent(
    override val id: String,
    override val timestamp: Long,
    val message: String,
    val level: String = "INFO",
) : DebugEvent

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

data class CrashEvent(
    override val id: String,
    override val timestamp: Long,
    val errorType: String,
    val message: String?,
    val stackTrace: String,
) : DebugEvent

// --- bus: an always-empty stream; emit does nothing ---
object DebugBus {
    val events: SharedFlow<DebugEvent> = MutableSharedFlow<DebugEvent>().asSharedFlow()
    suspend fun emit(event: DebugEvent) { /* no-op */ }
    fun tryEmit(event: DebugEvent): Boolean = false
}

// --- store: forever empty ---
class EventStore(capacity: Int = 300) {
    val events: StateFlow<List<DebugEvent>> = MutableStateFlow<List<DebugEvent>>(emptyList()).asStateFlow()
    fun record(event: DebugEvent) { /* no-op */ }
    fun clear() { /* no-op */ }
    fun snapshot(): List<DebugEvent> = emptyList()
}

// --- facade: installs nothing, redacts nothing, stores nothing ---
object DebugKit {
    val store: EventStore = EventStore()
    fun install() { /* no-op */ }
    fun redactHeaders(vararg names: String) { /* no-op */ }
    fun isHeaderRedacted(name: String): Boolean = false
    var maxBodyChars: Int = Int.MAX_VALUE
}

// --- producers: inert ---
object DebugLog {
    fun d(message: String) {}
    fun i(message: String) {}
    fun w(message: String) {}
    fun e(message: String) {}
}

object DebugCrashReporter {
    fun report(throwable: Throwable) {}
    fun crashEvent(throwable: Throwable): CrashEvent =
        CrashEvent(id = "", timestamp = 0, errorType = "", message = null, stackTrace = "")
}

// --- top-level helpers ---
const val REDACTED_PLACEHOLDER = "***REDACTED***"
fun HttpEvent.toCurl(): String = ""
fun nowMillis(): Long = 0L
fun nextEventId(): String = ""
fun installCrashHandler() { /* no-op */ }
fun redactHeaderMap(headers: Map<String, String>): Map<String, String> = headers

data class TruncatedBody(val text: String?, val truncated: Boolean)
fun truncateBody(body: String?, max: Int = Int.MAX_VALUE): TruncatedBody = TruncatedBody(body, false)
