package com.klarity.debugkit.core

/** JVM's answer to the `expect fun nowMillis()` contract. */
actual fun nowMillis(): Long = System.currentTimeMillis()
