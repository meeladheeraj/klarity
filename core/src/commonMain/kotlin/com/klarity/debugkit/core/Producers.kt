package com.klarity.debugkit.core

/**
 * Logger producer — emit log lines into the bus from anywhere, non-suspending.
 * (Events only reach the store if [DebugKit.install] has been called.)
 */
object DebugLog {
    fun d(message: String) = emit("DEBUG", message)
    fun i(message: String) = emit("INFO", message)
    fun w(message: String) = emit("WARN", message)
    fun e(message: String) = emit("ERROR", message)

    private fun emit(level: String, message: String) {
        DebugBus.tryEmit(
            LogEvent(id = nextEventId(), timestamp = nowMillis(), message = message, level = level)
        )
    }
}

/**
 * Crash producer — turns a [Throwable] into a [CrashEvent]. [report] is what the platform
 * crash hooks call; [crashEvent] is the pure builder (handy to test without throwing).
 */
object DebugCrashReporter {
    fun report(throwable: Throwable) {
        DebugBus.tryEmit(crashEvent(throwable))
    }

    fun crashEvent(throwable: Throwable): CrashEvent = CrashEvent(
        id = nextEventId(),
        timestamp = nowMillis(),
        errorType = throwable::class.simpleName ?: "Throwable",
        message = throwable.message,
        stackTrace = throwable.stackTraceToString(),
    )
}
