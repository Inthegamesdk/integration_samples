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
import ITGOverlayViewSwiftUI

struct PlayerViewModel {
    
    let avplayer: AVPlayer
    let videoView: AnyView
    var seekTimer: Timer? = nil
    
    init(_ videoUrl: URL) {
        self.avplayer = AVPlayer(url: videoUrl)
        self.videoView = AnyView(VideoPlayer(player: avplayer).ignoresSafeArea())
    }
    
    func seekTo(_ time: TimeInterval) {
        avplayer.seek(to: CMTime(value: CMTimeValue(time), timescale: 1), toleranceBefore: CMTime(value: CMTimeValue(0.1), timescale: 1), toleranceAfter: CMTime(value: CMTimeValue(0.1), timescale: 1), completionHandler: { _ in
            if avplayer.timeControlStatus == .paused {
                avplayer.play()
                avplayer.pause()
            }
        })
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
    @State var itgView: ITGOverlayViewSwiftUI<AnyView>? = nil
    @State private var observer: NSKeyValueObservation?
    @FocusState private var focusedItem: FocusableItem?
    
    var body: some View {
        ITGOverlayViewSwiftUI(
            channelSlug: channelSlug,
            accountId: accountId,
            environment: env,
            videoView: AnyView(playerViewModel.videoView.focused($focusedItem, equals: FocusableItem.player)),
            onOverlayRequestedVideoTime: {
               reportPlayerStatusToItgOverlay()
            },
            onOverlayRequestedPause: { playerViewModel.avplayer.pause() },
            onOverlayRequestedPlay: { playerViewModel.avplayer.play() },
            onOverlayRequestedFocus: { focusedItem = .itgOverlay },
            onOnOverlayReleasedFocus: { focusedItem = .player },
            onOverlayRequestedVideoSeek: playerViewModel.seekTo(_:),
            onOverlayRequestedVideoResolution: { playerViewModel.avplayer.currentItem?.presentationSize ?? .zero },
            onOverlayRequestedVideoLength: { playerViewModel.avplayer.currentItem?.duration.seconds ?? 0 },
            onOverlayRequestedVideoSoundLevel: { volume in playerViewModel.avplayer.volume = volume },
            onOverlayRequestedResetVideoSoundLevel: { playerViewModel.avplayer.volume = 1 },
            onOverlayCreated: { overlay in
                playerViewModel.avplayer.play()
                DispatchQueue.main.async {
                    self.itgView = overlay
                }
            }
        )
        .onReceive(NotificationCenter.default.publisher(for: AVPlayerItem.timeJumpedNotification)) { notification in
            if notification.object as? NSObject == playerViewModel.avplayer.currentItem {
                itgView?.videoPaused(playerViewModel.avplayer.currentTime().seconds)
                if playerViewModel.avplayer.timeControlStatus == .playing {
                    playerViewModel.seekTimer?.invalidate()
                    playerViewModel.seekTimer = Timer.scheduledTimer(withTimeInterval: 0.4, repeats: false, block: { _ in
                        playerViewModel.seekTimer = nil
                        if playerViewModel.avplayer.timeControlStatus == .playing {
                            itgView?.videoPlaying(playerViewModel.avplayer.currentTime().seconds)
                        }
                    })
                }
            }
        }
        .focused($focusedItem, equals: FocusableItem.itgOverlay)
        .onAppear {
            focusedItem = .player
            observer = playerViewModel.avplayer.observe(\.timeControlStatus, options: [.new]) { _, _ in
                self.reportPlayerStatusToItgOverlay()
            }
        }
        .ignoresSafeArea()
    }
    
    func reportPlayerStatusToItgOverlay() {
        if playerViewModel.avplayer.timeControlStatus == .playing {
            itgView?.videoPlaying(playerViewModel.avplayer.currentTime().seconds)
        } else {
            itgView?.videoPaused(playerViewModel.avplayer.currentTime().seconds)
        }
    }
}

#Preview {
    ContentView()
}
