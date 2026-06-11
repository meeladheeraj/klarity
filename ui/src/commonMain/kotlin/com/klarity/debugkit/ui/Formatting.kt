package com.klarity.debugkit.ui

/** Render a header map as `Key: Value` lines. Pure helper, kept out of the composables. */
internal fun Map<String, String>.asHeaderText(): String =
    entries.joinToString("\n") { "${it.key}: ${it.value}" }
