package com.klarity.debugkit.ktor

import com.klarity.debugkit.core.DebugBus
import com.klarity.debugkit.core.HttpEvent
import com.klarity.debugkit.core.nextEventId
import com.klarity.debugkit.core.nowMillis
import com.klarity.debugkit.core.redactHeaderMap
import com.klarity.debugkit.core.truncateBody
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText

/**
 * The capture half of the toolkit. Install this on any Ktor [io.ktor.client.HttpClient]
 * and every request flowing through it becomes an [HttpEvent] on the [DebugBus].
 *
 * `on(Send)` wraps the whole send: we stamp the start time, call `proceed(request)` to
 * let the real call happen, read the response off the resulting call, then emit. The
 * calling code never knows we're here — capture is fully decoupled from the app.
 */
val DebugKitPlugin = createClientPlugin("DebugKitPlugin") {
    on(Send) { request ->
        val start = nowMillis()
        val call = proceed(request)          // let the request actually go out
        val response = call.response

        // Headers: flatten each multi-value header to a single comma-joined string,
        // then redact any sensitive ones BEFORE building the event.
        val reqHeaders = request.headers.entries()
            .associate { (name, values) -> name to values.joinToString(", ") }
        val resHeaders = response.headers.entries()
            .associate { (name, values) -> name to values.joinToString(", ") }

        // Body: reading it consumes the stream. Safe here (MockEngine + caller ignores
        // the body). A production build would use Ktor's ResponseObserver to avoid this.
        val body = truncateBody(runCatching { response.bodyAsText() }.getOrNull())

        DebugBus.emit(
            HttpEvent(
                id = nextEventId(),
                timestamp = start,
                method = request.method.value,
                url = request.url.buildString(),
                statusCode = response.status.value,
                durationMs = nowMillis() - start,
                requestHeaders = redactHeaderMap(reqHeaders),
                responseHeaders = redactHeaderMap(resHeaders),
                responseBody = body.text,
                responseBodyTruncated = body.truncated,
            )
        )

        call // must return the call so the caller gets its response
    }
}
