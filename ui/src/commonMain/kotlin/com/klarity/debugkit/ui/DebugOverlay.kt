package com.klarity.debugkit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.klarity.debugkit.core.DebugKit
import com.klarity.debugkit.core.EventStore

/**
 * The public entry point of the overlay.
 *
 * Stateful wrapper (state hoisting): it collects the [store]'s flow and owns the
 * "which row is expanded" state, then delegates rendering to the stateless
 * [DebugOverlayContent].
 *
 * Depends on the [EventStore] abstraction (defaulting to [DebugKit.store]) rather than
 * reaching into the singleton directly — so it can be driven by any store in previews/tests.
 */
@Composable
fun DebugOverlay(store: EventStore = DebugKit.store) {
    val events by store.events.collectAsState()
    var selectedId by remember { mutableStateOf<String?>(null) }

    DebugOverlayContent(
        events = events,
        selectedId = selectedId,
        onToggle = { id -> selectedId = if (selectedId == id) null else id },
    )
}
