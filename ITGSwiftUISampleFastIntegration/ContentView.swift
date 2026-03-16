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
import ItgPlayerViewControllerSwiftUI
import ItgPlayerViewController

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
    @State var channelSlug: String = "demo"
    @State var accountId: String = "69230d1b5f7b3515524dd184"
    @State var env = ITGEnvironment(envName: "v2-7")
    @State var blockItg: Bool = false
    @State var itgPlayerViewController: ITGPlayerViewController?
    
    var body: some View {
        ITGPlayerViewControllerSwiftUI(channelSlug: channelSlug,
                                       accountId: accountId,
                                       environment: env,
                                       playerAdapter: ITGAVPlayerAdapter(playerViewModel.avplayer, playerView: UIHostingController(rootView: playerViewModel.videoView).view),
                                       onCreated: { itgPlayerViewController in
            DispatchQueue.main.async {
                self.itgPlayerViewController = itgPlayerViewController
#if os(iOS)
                self.itgPlayerViewController?.closeButtonVisibilityMode = .hidden
#endif
            }
        })
        .onChange(of: blockItg, {
            itgPlayerViewController?.overlayView?.block(blockItg, includePauseAd: true)
        })
        .ignoresSafeArea()
        .onAppear(perform: {
            playerViewModel.avplayer.play()
        })
    }
    
}

