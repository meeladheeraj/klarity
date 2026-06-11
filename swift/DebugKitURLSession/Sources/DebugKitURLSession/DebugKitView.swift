import SwiftUI
import DebugKit

/// A drop-in SwiftUI screen that shows captured network events live — the iOS counterpart
/// of the Compose overlay. Present it from a debug menu, a shake gesture, or a tab.
///
/// ```swift
/// .sheet(isPresented: $showDebug) { DebugKitView() }
/// ```
///
/// It refreshes whenever `DebugKitCapture` records (via `.debugKitDidRecord`).
public struct DebugKitView: View {

    @State private var events: [DebugKitDebugEvent] = []
    @State private var expandedId: String?

    public init() {}

    public var body: some View {
        NavigationView {
            List {
                ForEach(events.reversed(), id: \.id) { event in   // newest first
                    if let http = event as? DebugKitHttpEvent {
                        httpRow(http)
                    } else if let log = event as? DebugKitLogEvent {
                        Text("LOG  \(log.message)")
                            .font(.system(.footnote, design: .monospaced))
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Network · \(events.count)")
            .toolbar {
                Button("Clear") {
                    DebugKitDebugKit.shared.store.clear()
                    reload()
                }
            }
        }
        .onAppear(perform: reload)
        .onReceive(NotificationCenter.default.publisher(for: .debugKitDidRecord)) { _ in
            reload()
        }
    }

    @ViewBuilder
    private func httpRow(_ http: DebugKitHttpEvent) -> some View {
        let isOpen = expandedId == http.id
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 8) {
                Text(http.statusCode.map { "\($0.intValue)" } ?? "—")
                    .foregroundColor(statusColor(http.statusCode?.intValue))
                    .fontWeight(.bold)
                    .frame(width: 40, alignment: .leading)
                Text(http.method)
                    .fontWeight(.semibold)
                    .frame(width: 52, alignment: .leading)
                Text(http.url)
                    .font(.footnote)
                    .lineLimit(1)
                    .truncationMode(.middle)
                Spacer()
                Text("\(http.durationMs) ms")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            if isOpen {
                detailBlock("Request headers", headerText(http.requestHeaders))
                detailBlock("Response headers", headerText(http.responseHeaders))
                detailBlock("Response body", http.responseBody ?? "(none)")
            }
        }
        .padding(.vertical, 2)
        .contentShape(Rectangle())
        .onTapGesture { expandedId = isOpen ? nil : http.id }
    }

    private func detailBlock(_ title: String, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(.caption).bold().foregroundColor(.secondary)
            Text(body)
                .font(.system(.caption2, design: .monospaced))
                .textSelection(.enabled)
        }
        .padding(.top, 4)
    }

    private func headerText(_ headers: [String: String]) -> String {
        headers.map { "\($0.key): \($0.value)" }.sorted().joined(separator: "\n")
    }

    private func statusColor(_ code: Int32?) -> Color {
        guard let code else { return .gray }
        switch code {
        case ..<300: return .green
        case ..<400: return .blue
        case ..<500: return .orange
        default: return .red
        }
    }

    private func reload() {
        // store.record can fire off the main thread (URLProtocol delegate queue) — hop to main.
        DispatchQueue.main.async {
            events = DebugKitDebugKit.shared.store.snapshot()
        }
    }
}
