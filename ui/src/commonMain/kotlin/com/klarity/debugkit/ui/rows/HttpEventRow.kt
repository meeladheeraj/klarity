package com.klarity.debugkit.ui.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klarity.debugkit.core.HttpEvent
import com.klarity.debugkit.core.toCurl
import com.klarity.debugkit.ui.asHeaderText
import com.klarity.debugkit.ui.components.DetailBlock
import com.klarity.debugkit.ui.theme.statusColor

/** One HTTP request: a tappable summary line that expands to headers + body. */
@Composable
internal fun HttpEventRow(event: HttpEvent, expanded: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                event.statusCode?.toString() ?: "—",
                color = statusColor(event.statusCode),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp),
            )
            Text(event.method, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(56.dp))
            Text(event.url, maxLines = 1)
            Text("  ${event.durationMs}ms", color = Color.Gray)
        }
        if (expanded) {
            DetailBlock("Request headers", event.requestHeaders.asHeaderText())
            DetailBlock("Response headers", event.responseHeaders.asHeaderText())
            DetailBlock("Response body", event.responseBody ?: "(none)")
            DetailBlock("cURL", event.toCurl())
        }
    }
}
