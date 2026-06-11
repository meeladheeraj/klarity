package com.klarity.debugkit.core

/** What a redacted header value is replaced with. */
const val REDACTED_PLACEHOLDER = "***REDACTED***"

/**
 * Return a copy of [headers] with the value of any redacted header replaced.
 * Redaction happens here, in shared code, BEFORE the event reaches the store —
 * so a secret never lives in memory or gets streamed to the web viewer.
 */
fun redactHeaderMap(headers: Map<String, String>): Map<String, String> =
    headers.mapValues { (name, value) ->
        if (DebugKit.isHeaderRedacted(name)) REDACTED_PLACEHOLDER else value
    }

/** Result of trimming a body to a maximum length. */
data class TruncatedBody(val text: String?, val truncated: Boolean)

/** Trim [body] to [max] chars, appending a marker if anything was dropped. */
fun truncateBody(body: String?, max: Int = DebugKit.maxBodyChars): TruncatedBody {
    if (body == null) return TruncatedBody(null, false)
    return if (body.length > max) {
        TruncatedBody(body.take(max) + "…[truncated ${body.length - max} chars]", true)
    } else {
        TruncatedBody(body, false)
    }
}
