package com.klarity.debugkit.core

/** JVM's answer to the `expect fun nowMillis()` contract. */
actual fun nowMillis(): Long = System.currentTimeMillis()

actual fun installCrashHandler() {
    val previous = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        DebugCrashReporter.report(throwable)
        previous?.uncaughtException(thread, throwable) // preserve existing behaviour
    }
}
