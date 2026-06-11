package com.klarity.debugkit.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.klarity.debugkit.core.DebugKit
import com.klarity.debugkit.server.startDebugServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Desktop entry point: wire capture -> store, start live (mock) traffic, open window + web viewer. */
fun main() {
    DebugKit.install() // bus -> store

    // Traffic runs on a background scope so it never blocks the UI thread that `application` owns.
    val trafficScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    startFakeTraffic(trafficScope)

    // The web viewer: same store, streamed to any browser on the network. Open the printed URL.
    startDebugServer(DebugKit.store)

    application {
        Window(onCloseRequest = ::exitApplication, title = "Klarity Debug Toolkit") {
            DebugOverlay()
        }
    }
}
