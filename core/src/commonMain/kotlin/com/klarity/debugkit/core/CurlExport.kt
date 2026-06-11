package com.klarity.debugkit.core

/**
 * Render an [HttpEvent] as a copy-pasteable `curl` command (plan: "Share-as-cURL export").
 *
 * - Headers come from the stored event, so redacted ones (e.g. `Authorization`) show the
 *   placeholder — safe to share, but you'd restore the secret to actually replay it.
 * - Request bodies aren't captured yet, so `POST`/`PUT` payloads are not included.
 */
fun HttpEvent.toCurl(): String = buildString {
    append("curl -X ").append(method)
    for ((name, value) in requestHeaders) {
        append(" \\\n  -H '").append(name).append(": ").append(value.shellEscaped()).append("'")
    }
    append(" \\\n  '").append(url.shellEscaped()).append("'")
}

/** Escape a value for safe inclusion inside shell single-quotes (`'` -> `'\''`). */
private fun String.shellEscaped(): String = replace("'", "'\\''")
