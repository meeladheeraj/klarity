package com.klarity.debugkit.core

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS's answer to the `expect fun nowMillis()` contract — the second `actual`.
 *
 * `commonMain` doesn't change at all; iOS just supplies how it reads the clock, via
 * Foundation's NSDate. This is the whole KMP promise made concrete.
 */
actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
