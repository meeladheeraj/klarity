package com.klarity.debugkit.ui.rows

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.klarity.debugkit.core.LogEvent

/** One log line. */
@Composable
internal fun LogEventRow(event: LogEvent) {
    Text(
        "LOG   ${event.message}",
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    )
}
