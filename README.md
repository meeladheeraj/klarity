# DebugKit

**A Kotlin Multiplatform debug toolkit — HTTP capture for Ktor, an in-app Compose overlay, and a live web viewer you open in any browser.**

Capture runs on **JVM, Android, and iOS**. View traffic in an in-app overlay, or stream it live to any browser on your network — no install, no cable. (The browser viewer is hosted from a JVM process today; in-app hosting on Android/iOS is the next milestone — see below.)

> Status: **v0.1.0** · JVM · Android · iOS · *(early — APIs may change)*

---

## Why

KMP debug tooling is a known gap. The existing options are fragmented, and none offer a polished **live web viewer that works on iOS**. DebugKit's whole architecture is built around one idea:

> **Everything is a producer or consumer of a single event stream.**

Capture is fully decoupled from display, so the same captured events render in an in-app overlay *and* stream to a browser — and neither knows the other exists.

```
 [Ktor plugin]  [Logger]  [Crash hook]     <- producers
        \          |          /
         +----------------------+
         |   DebugBus           |  SharedFlow<DebugEvent>   <- the spine
         |   + EventStore       |  bounded ring buffer (StateFlow)
         +----------------------+
            /                  \
   [Compose overlay]      [Web viewer]      <- consumers
    (native window)      (browser, live)
```

---

## One-line setup

```kotlin
DebugKit.install()                                   // wire capture -> store (at app startup)

val client = HttpClient(engine) {
    install(DebugKitPlugin)                           // capture every request, automatically
}

DebugKit.redactHeaders("Authorization", "Cookie")     // secrets never hit the log
startDebugServer(DebugKit.store)                       // optional: live web viewer at :8080
```

Your calling code never references the toolkit again — capture happens invisibly in the Ktor pipeline.

---

## Install

Published with Gradle Module Metadata, so one coordinate resolves the right variant (JVM jar / Android AAR / iOS klib) for each consumer:

```kotlin
dependencies {
    debugImplementation("com.klarity.debugkit:core:0.1.0")
    debugImplementation("com.klarity.debugkit:interceptor-ktor:0.1.0")

    // The toolkit COMPILES OUT of release builds: identical API, empty bodies.
    releaseImplementation("com.klarity.debugkit:core-noop:0.1.0")
    releaseImplementation("com.klarity.debugkit:interceptor-ktor-noop:0.1.0")
}
```

Your app code is **identical** in both build types. Debug captures everything; release captures nothing and ships none of the engine.

**iOS / Swift consumers** use the prebuilt framework instead:

```swift
import DebugKit
DebugKitDebugKit.shared.install()
let events = DebugKitDebugKit.shared.store.snapshot()
```

---

## Usage example

A complete, realistic setup — wire it once at startup, then use your Ktor client normally.

```kotlin
import com.klarity.debugkit.core.DebugKit
import com.klarity.debugkit.core.DebugEvent
import com.klarity.debugkit.ktor.DebugKitPlugin
import com.klarity.debugkit.server.startDebugServer
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

// 1. At app startup — Application.onCreate() on Android, @main on iOS, or your KMP init.
fun initDebugging() {
    DebugKit.install()                                   // start piping events into the store
    DebugKit.redactHeaders("Authorization", "Cookie")     // strip secrets before they're stored
    DebugKit.maxBodyChars = 250_000                       // (optional) bound very large bodies
    startDebugServer(DebugKit.store)                      // (optional) live web viewer at :8080
}

// 2. Build your Ktor client ONCE with the plugin installed.
//    Pick your platform engine: CIO/OkHttp on Android & JVM, Darwin on iOS.
val httpClient = HttpClient(/* engine */) {
    install(DebugKitPlugin)                               // captures every request, invisibly
}

// 3. Use the client like normal — nothing here references the toolkit; capture just happens.
suspend fun loadUsers(): String =
    httpClient.get("https://api.example.com/users").bodyAsText()
```

Reading the captured events back:

```kotlin
// One-off snapshot (e.g. to dump on a crash):
val recent: List<DebugEvent> = DebugKit.store.snapshot()

// Or observe reactively — the list updates live as requests happen:
import androidx.compose.runtime.*
import com.klarity.debugkit.ui.DebugOverlay

@Composable
fun DebugScreen() {
    DebugOverlay()                                        // the built-in overlay UI
    // …or build your own from the same source of truth:
    // val events by DebugKit.store.events.collectAsState()
}
```

In a **release** build, the no-op artifacts replace `core` / `interceptor-ktor`: `install()` is empty and `DebugKitPlugin` captures nothing — **with zero changes to this code**. The web-viewer `server` is debug-only by nature; keep it as a `debugImplementation` and guard `startDebugServer(...)` behind your debug flag so it never ships to production.

### Using it from iOS (Swift)

On iOS, capture works two ways depending on how your app makes HTTP calls. Swift reads the captured events through the framework either way.

**1. Build & embed the framework:**

```bash
./gradlew :ios-framework:assembleDebugKitReleaseXCFramework
# → ios-framework/build/XCFrameworks/release/DebugKit.xcframework
```

Drag it into your Xcode target's *Frameworks, Libraries, and Embedded Content* (or wire it up via SPM / CocoaPods). For `URLSession` capture, also add the `DebugKitURLSession` Swift package (`swift/DebugKitURLSession`).

**2. Capture traffic — pick what matches your app:**

*(a) `URLSession` apps* — use the `DebugKitURLSession` package. One line at launch, no networking changes:

```swift
import DebugKitURLSession

DebugKitCapture.redactedHeaders = ["authorization", "cookie"]
DebugKitCapture.install()                       // auto-captures URLSession.shared

// For a custom-configured session:
let config = URLSessionConfiguration.default
DebugKitCapture.enable(on: config)
let session = URLSession(configuration: config)

// Or record manually from your own networking layer:
// DebugKitCapture.record(request:response:data:durationMs:)
```

*(b) Ktor (shared Kotlin)* — install the plugin on a Darwin-engine client in `iosMain`:

```kotlin
import io.ktor.client.engine.darwin.Darwin

// DebugKit.install() once at startup, then:
val httpClient = HttpClient(Darwin) { install(DebugKitPlugin) }
```

**3. View the captured events** — drop in the prebuilt SwiftUI screen (live list, tap-to-expand, Clear):

```swift
import DebugKitURLSession

.sheet(isPresented: $showDebug) { DebugKitView() }
```

…or read them yourself:

```swift
import DebugKit

let events = DebugKitDebugKit.shared.store.snapshot()
for case let http as DebugKitHttpEvent in events {
    print("\(http.method) \(http.url) → \(http.statusCode?.intValue ?? -1) (\(http.durationMs) ms)")
}
```

Notes for Swift consumers:
- Kotlin `object`s appear as `.shared`; classes carry the framework prefix (`DebugKitDebugKit`, `DebugKitHttpEvent`).
- `statusCode` is a boxed `KotlinInt?` (use `.intValue`); `durationMs` is an `Int64`.
- To observe the store **live** (not just `snapshot()`), collect the Kotlin `Flow` `store.events` — add [SKIE](https://skie.touchlab.co/) or KMP-NativeCoroutines for idiomatic Swift `async` / Combine bridging.

> **Note:** `URLSession` capture lives in the Swift `DebugKitURLSession` layer (a `URLProtocol` interceptor); the Kotlin `DebugKitPlugin` covers Ktor. Both record into the same `DebugKit.store`, so the overlay and viewer treat them identically.

---

## The web viewer (the differentiator)

`startDebugServer(DebugKit.store)` runs a tiny embedded server inside your app. Open **http://localhost:8080** (or the device's LAN IP) on any laptop on the same network:

- live request list, color-coded by status
- tap a row for headers + body
- zero install — it's just a WebSocket streaming JSON; the browser holds no state

> **Current state:** the viewer is hosted from a JVM process today (e.g. the desktop demo, `./gradlew :ui:run`). Embedding the server inside the Android/iOS app process — so you browse straight to the device — is the next milestone, and the reason the viewer is kept a stateless terminal.

---

## Modules

| Module | What it is | Targets |
|--------|-----------|---------|
| `core` | `DebugEvent` model, `DebugBus` (SharedFlow), `EventStore` (bounded ring buffer), `DebugKit` facade, redaction | JVM · Android · iOS |
| `interceptor-ktor` | Ktor client plugin → emits `HttpEvent`s | JVM · Android · iOS |
| `core-noop` / `interceptor-ktor-noop` | API-symmetric empty builds for release | JVM · Android · iOS |
| `ui` | Compose Multiplatform overlay (`DebugOverlay`) | Desktop (run it from the terminal) |
| `server` | Embedded Ktor server + WebSocket web viewer | JVM |
| `ios-framework` | Umbrella that packages `DebugKit.xcframework` | iOS |

---

## Design principles

1. **Decouple capture from display** — producers emit to a bus; they never know who's listening.
2. **One-line setup, zero config** — `DebugKit.install(...)`.
3. **No-op by API symmetry** — `debugImplementation` / `releaseImplementation`; the engine is absent from release binaries.
4. **Bounded memory** — a ring buffer (~300 events by default) so a debugger never OOMs its host.
5. **Redaction is first-class** — `DebugKit.redactHeaders(...)`; secrets are stripped before they reach the store.
6. **The web viewer is a dumb terminal** — all state lives in the app; the browser only renders the stream.

---

## Build & run

```bash
# Prereq for terminal builds (no system Java): use the JDK bundled with Android Studio.
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew :ui:run                                   # native overlay + web viewer at :8080
./gradlew :core:jvmTest                             # tests on JVM
./gradlew :core:iosSimulatorArm64Test               # the same tests on the iOS simulator
./gradlew assemble                                  # JVM jars + Android AARs
./gradlew :ios-framework:assembleDebugKitReleaseXCFramework   # DebugKit.xcframework
./gradlew publishToMavenLocal                       # publish to ~/.m2
./gradlew :noop-api-check:jvmTest                   # fail if core / core-noop APIs drift
```

> Always use `./gradlew` (the wrapper, pinned to Gradle 8.11.1), never a system `gradle`.

---

## Built with

Kotlin Multiplatform · Ktor (client + server) · Compose Multiplatform · kotlinx.serialization · kotlinx.coroutines
