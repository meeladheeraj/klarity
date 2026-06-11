import Foundation
import DebugKit
import FlyingFox

/// An embedded HTTP server that runs *inside the iOS app* and serves the network log to a
/// browser on the same Wi-Fi — the differentiator. Open the URL it prints on your laptop.
///
/// ```swift
/// DebugKitWebViewer.start()   // at launch (debug only); prints http://<phone-ip>:8080
/// ```
///
/// The page polls `/events.json` once a second (kept HTTP-only for simplicity; the browser
/// holds no state — all of it lives in `DebugKit.store`).
public enum DebugKitWebViewer {

    private static var task: Task<Void, Error>?

    public static func start(port: UInt16 = 8080) {
        #if DEBUG
        let server = HTTPServer(port: port)
        task = Task {
            await server.appendRoute("GET /") { _ in
                HTTPResponse(
                    statusCode: .ok,
                    headers: [.contentType: "text/html; charset=utf-8"],
                    body: Data(html.utf8)
                )
            }
            await server.appendRoute("GET /events.json") { _ in
                HTTPResponse(
                    statusCode: .ok,
                    headers: [.contentType: "application/json"],
                    body: eventsJSON()
                )
            }
            try await server.run()
        }
        print("[DebugKit] web viewer → http://\(localIPAddress() ?? "localhost"):\(port)")
        #endif
    }

    public static func stop() {
        task?.cancel()
        task = nil
    }

    // MARK: events -> JSON (Swift-side; mirrors the polymorphic "type" shape the page expects)

    private static func eventsJSON() -> Data {
        let events = DebugKitDebugKit.shared.store.snapshot()
        let array: [[String: Any]] = events.compactMap { event in
            if let http = event as? DebugKitHttpEvent {
                return [
                    "type": "http",
                    "id": http.id,
                    "method": http.method,
                    "url": http.url,
                    "statusCode": http.statusCode.map { Int($0.intValue) } ?? NSNull(),
                    "durationMs": Int(http.durationMs),
                    "requestHeaders": http.requestHeaders,
                    "responseHeaders": http.responseHeaders,
                    "responseBody": http.responseBody ?? NSNull(),
                ]
            } else if let log = event as? DebugKitLogEvent {
                return ["type": "log", "id": log.id, "message": log.message]
            }
            return nil
        }
        return (try? JSONSerialization.data(withJSONObject: array)) ?? Data("[]".utf8)
    }

    // MARK: device LAN IP (en0) so the printed URL is reachable from your laptop

    private static func localIPAddress() -> String? {
        var address: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>?
        guard getifaddrs(&ifaddr) == 0, let first = ifaddr else { return nil }
        defer { freeifaddrs(ifaddr) }

        var pointer: UnsafeMutablePointer<ifaddrs>? = first
        while let ptr = pointer {
            let interface = ptr.pointee
            if interface.ifa_addr.pointee.sa_family == UInt8(AF_INET),
               String(cString: interface.ifa_name) == "en0" {
                var addr = interface.ifa_addr.pointee
                var host = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                getnameinfo(&addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                            &host, socklen_t(host.count), nil, 0, NI_NUMERICHOST)
                address = String(cString: host)
            }
            pointer = interface.ifa_next
        }
        return address
    }

    // MARK: the page (polls /events.json; plain string concat in JS — no backticks)

    private static let html = """
    <!DOCTYPE html>
    <html lang="en">
    <head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>Klarity Debug Toolkit — Web Viewer</title>
    <style>
     body { font-family: -apple-system, system-ui, sans-serif; margin: 16px; background:#fafafa; color:#222; }
     h2 { font-weight:600; margin-bottom:4px; }
     #status { font-size:12px; color:#999; margin-bottom:12px; }
     table { width:100%; border-collapse:collapse; }
     th,td { text-align:left; padding:6px 10px; border-bottom:1px solid #eee; font-size:14px; }
     th { color:#888; font-weight:600; }
     td.status { font-weight:700; width:48px; }
     .mono { font-family: ui-monospace, Menlo, monospace; }
     .dur { color:#999; }
     tr.row { cursor:pointer; }
     tr.detail td { background:#f4f4f4; white-space:pre-wrap; font-family:ui-monospace,Menlo,monospace; font-size:12px; }
     .s2{color:#2E7D32}.s3{color:#1565C0}.s4{color:#E65100}.s5{color:#C62828}
    </style>
    </head>
    <body>
    <h2>Network <span id="count">0</span></h2>
    <div id="status">connecting…</div>
    <table>
     <thead><tr><th>Status</th><th>Method</th><th>URL</th><th>Time</th></tr></thead>
     <tbody id="rows"></tbody>
    </table>
    <script>
     var expanded = {};
     function cls(code){ if(code==null) return ''; if(code<300) return 's2'; if(code<400) return 's3'; if(code<500) return 's4'; return 's5'; }
     function esc(s){ return (s==null?'':String(s)).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
     function headersText(h){ if(!h) return ''; return Object.keys(h).map(function(k){ return k+': '+h[k]; }).join('\\n'); }
     function render(events){
       document.getElementById('count').textContent = events.length;
       document.getElementById('status').textContent = 'connected — live';
       var rows = document.getElementById('rows');
       rows.innerHTML = '';
       for (var i = events.length - 1; i >= 0; i--) {   // newest first
         var ev = events[i];
         if (ev.type === 'http') {
           var tr = document.createElement('tr');
           tr.className = 'row';
           tr.innerHTML = '<td class="status '+cls(ev.statusCode)+'">'+esc(ev.statusCode)+'</td>'
             + '<td>'+esc(ev.method)+'</td>'
             + '<td class="mono">'+esc(ev.url)+'</td>'
             + '<td class="dur">'+esc(ev.durationMs)+'ms</td>';
           (function(id){ tr.onclick = function(){ expanded[id] = !expanded[id]; render(events); }; })(ev.id);
           rows.appendChild(tr);
           if (expanded[ev.id]) {
             var d = document.createElement('tr'); d.className = 'detail';
             d.innerHTML = '<td colspan="4">'
               + 'Request headers\\n'  + esc(headersText(ev.requestHeaders))
               + '\\n\\nResponse headers\\n' + esc(headersText(ev.responseHeaders))
               + '\\n\\nResponse body\\n' + esc(ev.responseBody || '(none)')
               + '</td>';
             rows.appendChild(d);
           }
         } else if (ev.type === 'log') {
           var tr2 = document.createElement('tr');
           tr2.innerHTML = '<td>LOG</td><td colspan="3" class="mono">'+esc(ev.message)+'</td>';
           rows.appendChild(tr2);
         }
       }
     }
     function poll(){ fetch('/events.json').then(function(r){ return r.json(); }).then(render).catch(function(){ document.getElementById('status').textContent = 'disconnected'; }); }
     poll();
     setInterval(poll, 1000);
    </script>
    </body>
    </html>
    """
}
