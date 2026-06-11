package com.klarity.debugkit.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Bounded in-memory history of debug events — a ring buffer.
 *
 * Holds at most [capacity] events; when full, recording a new one drops the oldest
 * (FIFO eviction). This is design principle #4 — a debugger must never grow unbounded
 * and OOM the app it's inspecting.
 *
 * Exposes its contents as a [StateFlow] (not a SharedFlow): a *current value* that
 * changes. Any consumer that starts collecting — even much later — immediately gets
 * the full current list. That's what lets a late-joining web viewer show history.
 */
class EventStore(private val capacity: Int = 300) {

    private val _events = MutableStateFlow<List<DebugEvent>>(emptyList())

    /** The live, always-current list. Collect this from UI to react to changes. */
    val events: StateFlow<List<DebugEvent>> = _events.asStateFlow()

    /** Append an event, evicting the oldest if we're at capacity. */
    fun record(event: DebugEvent) {
        // `update` does an atomic compare-and-set loop, so concurrent producers are safe.
        _events.update { current ->
            val trimmed = if (current.size >= capacity) current.drop(1) else current
            trimmed + event
        }
    }

    /** Drop everything (e.g. a "clear" button in the viewer). */
    fun clear() = _events.update { emptyList() }

    /** A plain snapshot of the current contents, for one-off reads. */
    fun snapshot(): List<DebugEvent> = _events.value
}
