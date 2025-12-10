//
//  ContentView.swift
//  test
//
//  Created by ilya khymych on 26.11.2025.
//

import SwiftUI
import AVKit
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif
import ITGPlayerViewControllerSwiftUI
import ITGPlayerViewController

struct PlayerViewModel {
    
    let avplayer: AVPlayer
    let videoView: AnyView
    
    init(_ videoUrl: URL) {
        self.avplayer = AVPlayer(url: videoUrl)
        self.videoView = AnyView(VideoPlayer(player: avplayer).ignoresSafeArea())
    }

}

struct ContentView: View {
    
    enum FocusableItem: Hashable {
        case itgOverlay
        case player
    }

    @State var playerViewModel: PlayerViewModel = PlayerViewModel(URL(string: "https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4")!)
    @State var channelSlug = "samplechannel"
    @State var accountId = "68650da0324217d506bcc2d4"
    @State var env = ITGEnvironment(envName: "v2-3")
    @State var blockItg: Bool = false
    
    var body: some View {
        ITGPlayerViewControllerSwiftUI(channelSlug: channelSlug, accountId: accountId, environment: env, enableLogs: true, playerAdapter: ITGAVPlayerAdapter(playerViewModel.avplayer, playerView: UIHostingController(rootView: playerViewModel.videoView).view), blockAll: blockItg)
            .ignoresSafeArea()
            .onAppear(perform: {
                playerViewModel.avplayer.play()
            })
    }
    
}

#Preview {
    ContentView()
}

