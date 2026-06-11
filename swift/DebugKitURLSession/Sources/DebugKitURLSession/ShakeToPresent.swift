#if canImport(UIKit)
import SwiftUI
import UIKit

public extension Notification.Name {
    /// Posted when the device is shaken.
    static let debugKitDeviceDidShake = Notification.Name("com.klarity.debugkit.deviceDidShake")
}

// UIWindow sits at the top of the responder chain, so it receives the shake motion event
// no matter what's focused. Overriding this @objc method in an extension is allowed because
// it's imported from Objective-C (UIResponder).
extension UIWindow {
    open override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        if motion == .motionShake {
            NotificationCenter.default.post(name: .debugKitDeviceDidShake, object: nil)
        }
        super.motionEnded(motion, with: event)
    }
}

public extension View {
    /// Shake the device to present the DebugKit network log as a bottom sheet.
    /// Apply once, on your app's root view.
    func debugKitShakeToPresent() -> some View {
        modifier(DebugKitShakeModifier())
    }
}

private struct DebugKitShakeModifier: ViewModifier {
    @State private var isPresented = false

    func body(content: Content) -> some View {
        content
            .onReceive(NotificationCenter.default.publisher(for: .debugKitDeviceDidShake)) { _ in
                isPresented = true
            }
            .sheet(isPresented: $isPresented) {
                DebugKitView()
                    .presentationDetents([.medium, .large])      // ← bottom sheet
                    .presentationDragIndicator(.visible)
            }
    }
}
#endif
