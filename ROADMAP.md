# Roadmap

Tracks work **beyond v1**. v1 scope is the KMP toolkit per the project plan; see `README.md`.

## Phase 2 — Real crash reporting (committed)

Today's crash capture only catches **uncaught Kotlin exceptions** (JVM `Thread` handler / Kotlin/Native `setUnhandledExceptionHook`), and the store is **in-memory**, so:
- it does **not** catch Swift/Obj-C crashes, `fatalError`, or native signals — so it's effectively useless for a pure-Swift app (e.g. Track3d), and
- even when it fires, the event dies with the process unless a viewer received it live.

Phase 2 makes crash reporting actually useful:

1. **Native (Swift/Obj-C) crash capture** — in the `DebugKitURLSession` adapter:
   - `NSSetUncaughtExceptionHandler` for Obj-C `NSException`.
   - POSIX **signal handlers** (`SIGSEGV`, `SIGABRT`, `SIGILL`, `SIGBUS`, `SIGFPE`, `SIGTRAP`) for hard crashes.
   - Capture name, reason, and a (best-effort symbolicated) call stack into a `CrashEvent`.
   - Strong consideration: integrate a proven engine (PLCrashReporter / KSCrash) instead of hand-rolling async-signal-safe handlers — getting signal handling correct is genuinely hard.

2. **Persistence so crashes survive the process** — the prerequisite for any of the above to be viewable:
   - Write events (at minimum `CrashEvent`s) to disk on capture; reload on next launch so the overlay/viewer shows the last crash.
   - v1 deliberately skipped SQLDelight; Phase 2 adds a lightweight on-disk store (file/JSON append, or SQLDelight if querying grows).

> Net goal: a crash in Track3d (or any app) is captured, persisted, and visible in the overlay/web viewer on next launch — across Swift *and* Kotlin.

## Other beyond-v1 backlog

- **Android in-app web server** — the browser viewer currently runs on Desktop/JVM and iOS; add Android in-app hosting.
- **Compose Multiplatform overlay on Android/iOS** — `ui` is desktop-only today; the plan wants the shared overlay on device.
- **Week 2/3 polish** — cURL export; web-viewer filter/search.
- **Ship** — actual Maven Central upload (account + GPG key + publish plugin); demo GIF; launch post.
