package com.klarity.debugkit.core

import kotlin.test.Test
import kotlin.test.assertTrue

class CurlExportTest {

    @Test
    fun buildsCurlWithMethodHeadersAndUrl() {
        val event = HttpEvent(
            id = "1", timestamp = 0, method = "GET",
            url = "https://api.example.com/users", statusCode = 200, durationMs = 10,
            requestHeaders = mapOf("Accept" to "application/json"),
        )
        val curl = event.toCurl()
        assertTrue(curl.startsWith("curl -X GET"), curl)
        assertTrue(curl.contains("-H 'Accept: application/json'"), curl)
        assertTrue(curl.contains("'https://api.example.com/users'"), curl)
    }

    @Test
    fun escapesSingleQuotesInHeaderValues() {
        val event = HttpEvent(
            id = "2", timestamp = 0, method = "GET", url = "https://x/y",
            statusCode = 200, durationMs = 1,
            requestHeaders = mapOf("X-Note" to "it's fine"),
        )
        assertTrue(event.toCurl().contains("it'\\''s fine"), event.toCurl())
    }
}
