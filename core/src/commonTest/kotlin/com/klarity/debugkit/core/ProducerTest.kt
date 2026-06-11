package com.klarity.debugkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProducerTest {

    @Test
    fun crashEventCapturesTypeMessageAndStack() {
        val event = DebugCrashReporter.crashEvent(IllegalStateException("boom"))
        assertEquals("IllegalStateException", event.errorType)
        assertEquals("boom", event.message)
        assertTrue(event.stackTrace.isNotEmpty())
    }

    @Test
    fun crashEventHandlesNullMessage() {
        val event = DebugCrashReporter.crashEvent(RuntimeException())
        assertEquals("RuntimeException", event.errorType)
        assertEquals(null, event.message)
    }
}
