package com.klarity.debugkit.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Wraps the shared [DebugOverlay] composable in a [UIViewController] so a Swift/UIKit app
 * can present it (push, sheet, etc.). The UI itself is the *same* Compose code as desktop —
 * that's the Compose Multiplatform payoff.
 */
fun debugOverlayViewController(): UIViewController = ComposeUIViewController { DebugOverlay() }
