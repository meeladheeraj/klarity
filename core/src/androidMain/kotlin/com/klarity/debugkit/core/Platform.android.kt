package com.klarity.debugkit.core

/** Android's answer to the `expect fun nowMillis()` contract — the third `actual`. */
actual fun nowMillis(): Long = System.currentTimeMillis()
