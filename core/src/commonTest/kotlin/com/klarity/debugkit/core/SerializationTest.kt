package com.klarity.debugkit.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializationTest {

    @Test
    fun httpEventSerializesWithTypeDiscriminator() {
        val event: DebugEvent = HttpEvent(
            id = "1", timestamp = 0, method = "GET",
            url = "https://x/y", statusCode = 200, durationMs = 5,
        )
        val text = Json.encodeToString(event)
        // polymorphic JSON tags the subtype via @SerialName — what the web viewer switches on
        assertTrue(text.contains("\"type\":\"http\""), "missing type discriminator in: $text")
        assertTrue(text.contains("\"method\":\"GET\""))
    }

    @Test
    fun httpEventRoundTrips() {
        val original: DebugEvent = HttpEvent(
            id = "2", timestamp = 42, method = "POST",
            url = "https://api/login", statusCode = 401, durationMs = 9,
        )
        val back = Json.decodeFromString<DebugEvent>(Json.encodeToString(original))
        assertTrue(back is HttpEvent)
        assertEquals(original, back)
    }

    @Test
    fun crashEventSerializesAndRoundTrips() {
        val original: DebugEvent = CrashEvent(
            id = "9", timestamp = 1, errorType = "IllegalStateException",
            message = "boom", stackTrace = "at x\nat y",
        )
        val text = Json.encodeToString(original)
        assertTrue(text.contains("\"type\":\"crash\""), "missing discriminator in: $text")
        val back = Json.decodeFromString<DebugEvent>(text)
        assertEquals(original, back)
    }
}
