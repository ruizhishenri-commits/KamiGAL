import SwiftUI

@main
struct KamiGALApp: App {
    @StateObject private var gameStore = GameStore()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(gameStore)
                .preferredColorScheme(.dark)
        }
    }
}