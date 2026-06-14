package com.klarity.debugkit.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path

private val persistenceJson = Json { ignoreUnknownKeys = true; encodeDefaults = true }

/** Serialize the event list to JSON (used for on-disk persistence). */
internal fun encodeEvents(events: List<DebugEvent>): String = persistenceJson.encodeToString(events)

/** Parse a persisted JSON list back into events; tolerant of corrupt/empty files. */
internal fun decodeEvents(text: String): List<DebugEvent> =
    runCatching { persistenceJson.decodeFromString<List<DebugEvent>>(text) }.getOrDefault(emptyList())

/** Write [events] to [file] (creating parent dirs). Pure of which FileSystem, so it's testable. */
internal fun persistEvents(fs: FileSystem, file: Path, events: List<DebugEvent>) {
    file.parent?.let { fs.createDirectories(it) }
    fs.write(file) { writeUtf8(encodeEvents(events)) }
}

/** Read previously persisted events from [file], or empty if missing/unreadable. */
internal fun loadEvents(fs: FileSystem, file: Path): List<DebugEvent> {
    if (!fs.exists(file)) return emptyList()
    return runCatching { fs.read(file) { readUtf8() } }
        .map { decodeEvents(it) }
        .getOrDefault(emptyList())
}
