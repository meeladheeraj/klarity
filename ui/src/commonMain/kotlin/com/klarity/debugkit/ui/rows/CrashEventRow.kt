package com.klarity.debugkit.ui.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klarity.debugkit.core.CrashEvent
import com.klarity.debugkit.ui.components.DetailBlock

/** One captured crash: type + message, expanding to the full stack trace. */
@Composable
internal fun CrashEventRow(event: CrashEvent, expanded: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Row {
            Text("CRASH", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
            Text("  ${event.errorType}", fontWeight = FontWeight.SemiBold)
            event.message?.let { Text("  $it", maxLines = 1) }
        }
        if (expanded) {
            DetailBlock("Stack trace", event.stackTrace)
        }
    }
}
