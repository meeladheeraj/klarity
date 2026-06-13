# Launch post — drafts

> DRAFT for v0.1.0. Repo: https://github.com/meeladheeraj/klarity
> **Published to Maven Central** as `io.github.meeladheeraj:<module>:0.1.0`.
> Honesty guardrails (don't overclaim): capture is **Ktor-only** in the KMP core; the
> browser viewer runs **JVM-hosted today** (in-app device hosting is the next milestone);
> a separate **Swift adapter** covers `URLSession` apps + an in-app iOS web server.

---

## Tagline

**DebugKit** — a Kotlin Multiplatform network debugger you can watch *live in your browser*.
Capture Ktor traffic once in shared code; view it in an in-app Compose overlay, or stream it
to any browser on your Wi-Fi. Android, iOS, JVM.

---

## r/Kotlin / Kotlin Slack (technical audience)

**Title:** DebugKit v0.1.0 — a KMP debug toolkit (Ktor capture + Compose overlay + live web viewer)

KMP debug tooling is still a gap — the options are fragmented and there's no polished,
trusted default. So I started **DebugKit**.

The design is one idea: **everything is a producer or consumer of a single event stream.**
A Ktor client plugin captures requests into a shared `commonMain` store; consumers render it.
Today that's two consumers:

- an **in-app Compose Multiplatform overlay** (desktop / Android / iOS) — color-coded request
  list, tap for headers/body, copy as cURL;
- a **live web viewer** — an embedded server streams the log to your browser over a WebSocket,
  with filter/search. Open a URL on the same Wi-Fi, zero install.

One-line setup, and it compiles **out** of release builds:
```kotlin
DebugKit.install()
val client = HttpClient(engine) { install(DebugKitPlugin) }
DebugKit.redactHeaders("Authorization")   // redaction is first-class
```
Release builds link API-symmetric **no-op** artifacts (`debugImplementation` /
`releaseImplementation`), so there's zero overhead and no captured data in production — enforced
by a build-time API-parity check.

**In v0.1.0:** Ktor HTTP capture (headers/body/timing/status) + redaction; bounded in-memory
ring buffer; log + crash producers; Compose overlay on desktop/Android/iOS; live web viewer with
filter/search; cURL export; JVM/Android/iOS targets + an iOS XCFramework.

**Honest status:** it's early. The browser viewer currently runs from a JVM host (the desktop
app); **hosting it inside the Android/iOS app process — so you browse straight to the device — is
the next milestone** (there's already a Swift adapter that does this for `URLSession`/iOS apps).
Native crash capture + persistence are on the roadmap.

Feedback and issues very welcome 👉 https://github.com/meeladheeraj/klarity

---

## LinkedIn (broader framing)

Debugging network calls in a Kotlin Multiplatform app is still clunky — especially on iOS.

So I've been building **DebugKit**: capture your app's HTTP traffic once in shared Kotlin, then
*see* it — in an in-app overlay, or streamed live to any browser on the same Wi-Fi. No cable, no
install: open a URL and watch requests appear in real time.

It's built around a single idea — one event stream, many viewers — which keeps capture fully
decoupled from display, and lets the whole thing **compile out of release builds** so users never
pay for it.

v0.1.0 is early and open source. If you write KMP (or want to), I'd love your feedback.
🔗 https://github.com/meeladheeraj/klarity

#KotlinMultiplatform #Kotlin #MobileDev #OpenSource

---

## Demo GIF / video (the most important asset — plan: "demo matters more than feature count")

Lead with the **differentiator moment**, not a feature tour. Split-screen, ~10–15s:

1. Left: the app (phone/emulator or desktop) making a few requests.
2. Right: a laptop browser at `http://<device>:8080` — rows appear **live** as the requests fire.
3. Click a row → headers/body expand. Type in the filter → list narrows.

The single frame that sells it: *"my phone's traffic, in my browser, with nothing installed."*
Keep it under 15s; loop it. A 30-sec narrated video can follow for the post body.
