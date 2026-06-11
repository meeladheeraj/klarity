package com.klarity.debugkit.server

import com.klarity.debugkit.core.DebugEvent
import com.klarity.debugkit.core.EventStore
import io.ktor.http.ContentType
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { encodeDefaults = true }

/**
 * Starts an embedded web server (non-blocking) that serves the viewer page at `/` and
 * streams [store] over a WebSocket at `/events`. The browser holds no state — every change
 * to the store's StateFlow is serialized to JSON and pushed down the socket.
 */
fun startDebugServer(store: EventStore, port: Int = 8080) {
    embeddedServer(CIO, port = port) {
        install(WebSockets)
        routing {
            get("/") {
                call.respondText(WEB_VIEWER_HTML, ContentType.Text.Html)
            }
            webSocket("/events") {
                try {
                    // collect never completes — it streams the current list and every update.
                    store.events.collect { events: List<DebugEvent> ->
                        send(Frame.Text(json.encodeToString(events)))
                    }
                } catch (_: Throwable) {
                    // client disconnected; let the handler coroutine end quietly.
                }
            }
        }
    }.start(wait = false)
    println("[server] web viewer at http://localhost:$port")
}
