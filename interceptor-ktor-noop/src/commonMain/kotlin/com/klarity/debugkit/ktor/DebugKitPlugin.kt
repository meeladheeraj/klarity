package com.klarity.debugkit.ktor

import io.ktor.client.plugins.api.createClientPlugin

/**
 * NO-OP build of the capture plugin. Same public symbol as the real one, so app code
 * (`client.install(DebugKitPlugin)`) is unchanged — but it registers no hooks, so not a
 * single request is observed. The real capture engine is absent from the release binary.
 */
val DebugKitPlugin = createClientPlugin("DebugKitPlugin") {
    // intentionally empty — no on(Send), no capture
}
