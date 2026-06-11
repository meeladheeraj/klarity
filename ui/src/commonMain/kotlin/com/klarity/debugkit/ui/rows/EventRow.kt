package com.klarity.debugkit.ui.rows

import androidx.compose.runtime.Composable
import com.klarity.debugkit.core.CrashEvent
import com.klarity.debugkit.core.DebugEvent
import com.klarity.debugkit.core.HttpEvent
import com.klarity.debugkit.core.LogEvent

/**
 * Dispatches a [DebugEvent] to the right row composable. The exhaustive `when` over the
 * sealed type means adding a new event kind (e.g. CrashEvent) makes this fail to compile
 * until you add its row — the compiler keeps the UI honest.
 */
@Composable
internal fun EventRow(event: DebugEvent, expanded: Boolean, onClick: () -> Unit) {
    when (event) {
        is HttpEvent -> HttpEventRow(event, expanded, onClick)
        is LogEvent -> LogEventRow(event)
        is CrashEvent -> CrashEventRow(event, expanded, onClick)
    }
}
