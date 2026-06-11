package com.klarity.debugkit.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Maps an HTTP status code to a display color.
 *
 * Isolated here (Open/Closed): new coloring rules go in this one function — no row
 * composable ever needs to change to recolor statuses.
 */
internal fun statusColor(code: Int?): Color = when {
    code == null -> Color.Gray
    code < 300 -> Color(0xFF2E7D32) // green  – success
    code < 400 -> Color(0xFF1565C0) // blue   – redirect
    code < 500 -> Color(0xFFE65100) // orange – client error
    else -> Color(0xFFC62828)       // red    – server error
}
