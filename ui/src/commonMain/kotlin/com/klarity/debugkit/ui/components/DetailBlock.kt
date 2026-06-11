package com.klarity.debugkit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** A reusable labeled block: a bold title over monospaced content. */
@Composable
internal fun DetailBlock(title: String, content: String) {
    Column(Modifier.padding(top = 6.dp, start = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
        Text(content, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
    }
}
