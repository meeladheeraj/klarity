import Foundation

/// Transparently captures `URLSession` traffic into DebugKit.
///
/// It re-issues the request through a private session (so the app gets its normal
/// response), accumulates the body, and on completion hands everything to
/// `DebugKitCapture.record(...)`. A marker property prevents infinite recursion.
public final class DebugKitURLProtocol: URLProtocol, URLSessionDataDelegate {

    private static let handledKey = "com.klarity.debugkit.handled"

    private var session: URLSession?
    private var dataTask: URLSessionDataTask?
    private var startTime = Date()
    private var accumulatedData = Data()
    private var capturedResponse: HTTPURLResponse?

    // MARK: URLProtocol

    public override class func canInit(with request: URLRequest) -> Bool {
        // Don't re-handle our own re-issued request, and only touch http(s).
        if URLProtocol.property(forKey: handledKey, in: request) != nil { return false }
        guard let scheme = request.url?.scheme?.lowercased(),
              scheme == "http" || scheme == "https" else { return false }
        return true
    }

    public override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    public override func startLoading() {
        startTime = Date()

        guard let mutable = (request as NSURLRequest).mutableCopy() as? NSMutableURLRequest else {
            client?.urlProtocol(self, didFailWithError: URLError(.unknown))
            return
        }
        // Tag it so canInit(_:) returns false for the re-issued request (no recursion).
        URLProtocol.setProperty(true, forKey: Self.handledKey, in: mutable)

        // A private session whose config does NOT include this protocol.
        let config = URLSessionConfiguration.default
        session = URLSession(configuration: config, delegate: self, delegateQueue: nil)
        dataTask = session?.dataTask(with: mutable as URLRequest)
        dataTask?.resume()
    }

    public override func stopLoading() {
        dataTask?.cancel()
        session?.invalidateAndCancel()
    }

    // MARK: URLSessionDataDelegate (forward to the original client + accumulate for capture)

    public func urlSession(_ session: URLSession,
                           dataTask: URLSessionDataTask,
                           didReceive response: URLResponse,
                           completionHandler: @escaping (URLSession.ResponseDisposition) -> Void) {
        capturedResponse = response as? HTTPURLResponse
        client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
        completionHandler(.allow)
    }

    public func urlSession(_ session: URLSession,
                           dataTask: URLSessionDataTask,
                           didReceive data: Data) {
        accumulatedData.append(data)
        client?.urlProtocol(self, didLoad: data)
    }

    public func urlSession(_ session: URLSession,
                           task: URLSessionTask,
                           didCompleteWithError error: Error?) {
        if let error = error {
            client?.urlProtocol(self, didFailWithError: error)
        } else {
            client?.urlProtocolDidFinishLoading(self)
        }

        if let response = capturedResponse {
            let durationMs = Int64(Date().timeIntervalSince(startTime) * 1000)
            DebugKitCapture.record(
                request: request,
                response: response,
                data: accumulatedData,
                durationMs: durationMs
            )
        }
        session.finishTasksAndInvalidate()
    }
}
