package com.klarity.debugkit.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * The spine of the whole toolkit: one hot, broadcast stream of [DebugEvent]s.
 *
 * - `_events` is the writable side — only this file can emit.
 * - `events` is the public read-only view every consumer collects from.
 *
 * `extraBufferCapacity = 64` lets producers emit a small burst without suspending
 * even if a consumer is briefly slow. `replay = 0` means new subscribers don't
 * get history — they only see events emitted after they start collecting.
 */
object DebugBus {
    private val _events = MutableSharedFlow<DebugEvent>(
        replay = 0,
        extraBufferCapacity = 64,
    )

    val events: SharedFlow<DebugEvent> = _events.asSharedFlow()

    suspend fun emit(event: DebugEvent) {
        _events.emit(event)
    }

    /**
     * Non-suspending emit for fire-and-forget producers (logger, crash hook) that can't
     * suspend. Succeeds as long as buffer space is free; returns false if it would block.
     */
    fun tryEmit(event: DebugEvent): Boolean = _events.tryEmit(event)
}
