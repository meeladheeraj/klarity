# DebugKitURLSession — optional iOS / URLSession adapter

> **Scope: this is NOT part of the core KMP v1.** It's an optional Swift adapter for apps
> that are *not* Kotlin Multiplatform and capture HTTP via `URLSession` (e.g. a UIKit/SwiftUI
> app). The main toolkit's `README` (repo root) describes the canonical KMP product.

## Why it exists (and why it's separate)

The DebugKit **plan** is deliberately scoped:

- Capture is **Ktor-only**; native `URLSession` interception is *explicitly out of scope for v1*.
- The in-app overlay is **Compose Multiplatform** (shared Kotlin), including on iOS.
- The web viewer is the shared **Ktor `server`** module.

A pure-Swift / `URLSession` app (no Ktor, no shared Kotlin) can't use any of that. This adapter
fills that gap **outside** the KMP product, by re-implementing the consumer side in Swift on top
of the `DebugKit` XCFramework (which still owns the event model + store):

| File | Role | KMP equivalent it stands in for |
|------|------|---------------------------------|
| `DebugKitCapture` / `DebugKitURLProtocol` | capture `URLSession` traffic | the Ktor `interceptor-ktor` plugin |
| `DebugKitView` | SwiftUI log screen | the Compose Multiplatform overlay |
| `DebugKitWebViewer` (FlyingFox) | in-app browser viewer | the Ktor `server` module |

So it intentionally duplicates functionality in Swift. That's the trade-off of supporting
non-KMP apps; it is **not** the "write once" KMP story and should be treated as an add-on.

## Status

Functional but **not compile-verified in CI** (it builds in an Xcode/iOS target, not via the
Gradle build). Treat as experimental. For a real KMP app, prefer the shared Kotlin path in the
root README — no Swift required.

## Usage

See the root `README.md` → "Using it from iOS (Swift)".
