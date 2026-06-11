package com.klarity.debugkit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klarity.debugkit.core.DebugEvent
import com.klarity.debugkit.ui.rows.EventRow

/**
 * The pure, stateless overlay UI: given the data and the current selection, render it.
 * Holds no state of its own — easy to preview and reason about. State lives in [DebugOverlay].
 */
@Composable
internal fun DebugOverlayContent(
    events: List<DebugEvent>,
    selectedId: String?,
    onToggle: (String) -> Unit,
) {
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    "Network · ${events.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(events, key = { it.id }) { event ->
                        EventRow(
                            event = event,
                            expanded = selectedId == event.id,
                            onClick = { onToggle(event.id) },
                        )
                    }
                }
            }
        }
    }
}
