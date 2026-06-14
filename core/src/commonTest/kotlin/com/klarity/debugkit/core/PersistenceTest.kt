package com.klarity.debugkit.core

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistenceTest {

    @Test
    fun eventsRoundTripThroughDisk() {
        val fs = FakeFileSystem()
        val file = "/debug/events.json".toPath()
        val events: List<DebugEvent> = listOf(
            HttpEvent(id = "1", timestamp = 0, method = "GET", url = "https://x/y", statusCode = 200, durationMs = 5),
            LogEvent(id = "2", timestamp = 1, message = "hi", level = "WARN"),
            CrashEvent(id = "3", timestamp = 2, errorType = "IllegalStateException", message = "boom", stackTrace = "at x"),
        )

        persistEvents(fs, file, events)        // write (creates /debug)
        val loaded = loadEvents(fs, file)      // read back

        assertEquals(events, loaded)
        fs.checkNoOpenFiles()
    }

    @Test
    fun loadMissingFileReturnsEmpty() {
        assertTrue(loadEvents(FakeFileSystem(), "/nope.json".toPath()).isEmpty())
    }

    @Test
    fun decodeToleratesGarbage() {
        assertTrue(decodeEvents("not json {{{").isEmpty())
    }
}
