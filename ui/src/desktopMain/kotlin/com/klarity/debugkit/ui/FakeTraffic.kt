package com.klarity.debugkit.ui

import com.klarity.debugkit.ktor.DebugKitPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** A canned endpoint the fake server knows how to answer. */
private data class Route(val method: String, val path: String, val status: Int, val body: String)

private val routes = listOf(
    Route("GET", "/users", 200, """{"users":[{"id":1,"name":"Ada"}]}"""),
    Route("POST", "/login", 401, """{"error":"invalid credentials"}"""),
    Route("GET", "/orders", 200, """{"orders":[101,102,103]}"""),
    Route("DELETE", "/cache", 204, ""),
    Route("GET", "/reports/heavy", 500, """{"error":"internal server error"}"""),
)

/**
 * Starts an endless loop (on [scope], a background dispatcher) that fires one mock request
 * every ~1.2s. Each call flows through the real [DebugKitPlugin], so events land in the
 * store and the overlay updates live. This stands in for a host app's real HTTP traffic.
 */
internal fun startFakeTraffic(scope: CoroutineScope) {
    // A fake server: look up the route by path, return its status + body.
    val engine = MockEngine { request ->
        val route = routes.firstOrNull { it.path == request.url.encodedPath }
        respond(
            content = route?.body ?: "{}",
            status = HttpStatusCode.fromValue(route?.status ?: 200),
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }
    val client = HttpClient(engine) { install(DebugKitPlugin) }

    scope.launch {
        delay(300) // let DebugKit's bus->store collector subscribe before the first event
        var i = 0
        while (true) {
            val route = routes[i % routes.size]
            client.request("https://api.example.com${route.path}") {
                method = HttpMethod.parse(route.method)
                headers { append("Authorization", "Bearer super-secret-token") }
            }
            i++
            delay(1200)
        }
    }
}
