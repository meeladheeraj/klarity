package com.klarity.debugkit.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventStoreTest {

    private fun log(i: Int) = LogEvent(id = "id-$i", timestamp = i.toLong(), message = "msg $i")

    @Test
    fun keepsEverythingUnderCapacity() {
        val store = EventStore(capacity = 3)
        store.record(log(0))
        store.record(log(1))
        assertEquals(2, store.snapshot().size)
    }

    @Test
    fun evictsOldestWhenFull() {
        val store = EventStore(capacity = 3)
        repeat(5) { store.record(log(it)) }
        // oldest two (id-0, id-1) evicted; newest three kept in order
        assertEquals(listOf("id-2", "id-3", "id-4"), store.snapshot().map { it.id })
    }

    @Test
    fun clearEmptiesTheStore() {
        val store = EventStore(capacity = 3)
        store.record(log(0))
        store.clear()
        assertTrue(store.snapshot().isEmpty())
    }
}
