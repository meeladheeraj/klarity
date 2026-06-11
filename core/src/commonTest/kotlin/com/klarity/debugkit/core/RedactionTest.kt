package com.klarity.debugkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RedactionTest {

    @Test
    fun redactsConfiguredHeaderButKeepsOthers() {
        DebugKit.redactHeaders("Authorization")
        val out = redactHeaderMap(mapOf("Authorization" to "Bearer secret", "X-Trace" to "abc"))
        assertEquals(REDACTED_PLACEHOLDER, out["Authorization"])
        assertEquals("abc", out["X-Trace"])
    }

    @Test
    fun redactionIsCaseInsensitive() {
        DebugKit.redactHeaders("cookie")
        val out = redactHeaderMap(mapOf("Cookie" to "session=1"))
        assertEquals(REDACTED_PLACEHOLDER, out["Cookie"])
    }

    @Test
    fun truncatesBodyOverLimitAndMarksIt() {
        val result = truncateBody("0123456789", max = 4)
        assertTrue(result.truncated)
        assertTrue(result.text!!.startsWith("0123"))
    }

    @Test
    fun keepsShortBodyIntact() {
        val result = truncateBody("hi", max = 100)
        assertEquals("hi", result.text)
        assertFalse(result.truncated)
    }
}
