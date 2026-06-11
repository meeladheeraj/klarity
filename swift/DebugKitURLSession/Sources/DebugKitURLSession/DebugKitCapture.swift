import Foundation
import DebugKit

public extension Notification.Name {
    /// Posted after any event is recorded, so a live UI can refresh from `store.snapshot()`.
    static let debugKitDidRecord = Notification.Name("com.klarity.debugkit.didRecord")
}

/// The single entry point for capturing `URLSession` traffic into DebugKit on iOS.
///
/// Two ways to use it — both funnel through `record(...)`, so the resulting events are
/// identical and show up the same way in `DebugKit.store`:
///
///   • Option B (automatic): call `DebugKitCapture.install()` once at launch. Every
///     `URLSession.shared` request is intercepted by a `URLProtocol` — no per-call code.
///     For custom-configured sessions, call `DebugKitCapture.enable(on: config)`.
///
///   • Option A (manual): call `DebugKitCapture.record(request:response:data:durationMs:)`
///     yourself from your networking layer.
public enum DebugKitCapture {

    /// Header names (lower-cased) whose values are replaced before storing. Configure once.
    public static var redactedHeaders: Set<String> = ["authorization", "cookie", "set-cookie"]

    // MARK: Option B — automatic interception

    /// Register the interceptor globally. Affects `URLSession.shared`. Call once at startup.
    public static func install() {
        URLProtocol.registerClass(DebugKitURLProtocol.self)
    }

    /// For sessions built from a custom `URLSessionConfiguration` (registerClass doesn't
    /// reach those), insert the interceptor into the config's protocol chain.
    public static func enable(on configuration: URLSessionConfiguration) {
        var classes = configuration.protocolClasses ?? []
        classes.insert(DebugKitURLProtocol.self, at: 0)
        configuration.protocolClasses = classes
    }

    // MARK: Option A — manual recording (also used internally by the URLProtocol)

    public static func record(request: URLRequest,
                              response: HTTPURLResponse,
                              data: Data,
                              durationMs: Int64) {
        #if DEBUG
        let reqHeaders = redact(request.allHTTPHeaderFields ?? [:])

        var rawResHeaders: [String: String] = [:]
        for (key, value) in response.allHeaderFields { rawResHeaders["\(key)"] = "\(value)" }
        let resHeaders = redact(rawResHeaders)

        let event = DebugKitHttpEvent(
            id: UUID().uuidString,
            timestamp: Int64(Date().timeIntervalSince1970 * 1000),
            method: request.httpMethod ?? "GET",
            url: request.url?.absoluteString ?? "",
            statusCode: KotlinInt(int: Int32(response.statusCode)),
            durationMs: durationMs,
            requestHeaders: reqHeaders,
            responseHeaders: resHeaders,
            responseBody: String(data: data, encoding: .utf8),
            responseBodyTruncated: false
        )
        DebugKitDebugKit.shared.store.record(event: event)
        NotificationCenter.default.post(name: .debugKitDidRecord, object: nil)
        #endif
    }

    private static func redact(_ headers: [String: String]) -> [String: String] {
        guard !redactedHeaders.isEmpty else { return headers }
        var out = headers
        for key in headers.keys where redactedHeaders.contains(key.lowercased()) {
            out[key] = "***REDACTED***"
        }
        return out
    }
}
