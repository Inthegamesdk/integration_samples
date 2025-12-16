//
//  ContentView.swift
//  test
//
//  Created by ilya khymych on 26.11.2025.
//

import SwiftUI
import Combine
import AVKit
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif
import ITGOverlayViewSwiftUI

class PlayerViewModel: ObservableObject {
    
    @Published var isPlaying: Bool = false
    let avplayer: AVPlayer
    let videoView: any View
    var seekTimer: Timer? = nil
    var observer: NSKeyValueObservation?
    
    init(_ videoUrl: URL) {
        self.avplayer = AVPlayer(url: videoUrl)
        self.videoView = VideoPlayer(player: avplayer).ignoresSafeArea()
        observer = avplayer.observe(\.timeControlStatus, options: [.new]) { [weak self] _, _ in
            guard let self = self else { return }
            self.isPlaying = self.avplayer.timeControlStatus == .playing
        }
        NotificationCenter.default.addObserver(self, selector: #selector(timeJumped(_:)), name: AVPlayerItem.timeJumpedNotification, object: nil)
    }
    
    @objc private func timeJumped(_ notification: Notification) {
        guard let currentItem = avplayer.currentItem, notification.object as? AVPlayerItem === currentItem else { return }
        self.isPlaying = false
        if avplayer.timeControlStatus == .playing {
            seekTimer?.invalidate()
            seekTimer = Timer.scheduledTimer(withTimeInterval: 0.4, repeats: false, block: { [weak self] _ in
                guard let self = self else { return }
                self.seekTimer = nil
                if self.avplayer.timeControlStatus == .playing {
                    self.isPlaying = true
                }
            })
        }
    }

    func seekTo(_ time: TimeInterval) {
        avplayer.seek(to: CMTime(value: CMTimeValue(time), timescale: 1), toleranceBefore: CMTime(value: CMTimeValue(0.1), timescale: 1), toleranceAfter: CMTime(value: CMTimeValue(0.1), timescale: 1), completionHandler: { [weak self] _ in
            guard let self = self else { return }
            if self.avplayer.timeControlStatus == .paused {
                self.avplayer.play()
                self.avplayer.pause()
            }
        })
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        observer?.invalidate()
        seekTimer?.invalidate()
    }
}

struct ContentView: View {
    
    enum FocusableItem: Hashable {
        case itgOverlay
        case player
    }
    
    @State var blockItg: Bool = false
    @StateObject var playerViewModel = PlayerViewModel(URL(string: "https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4")!)
    @State var channelSlug = "samplechannel"
    @State var accountId = "68650da0324217d506bcc2d4"
    @State var env = ITGEnvironment(envName: "v2-3")
    @FocusState private var focusedItem: FocusableItem?

    var body: some View {
        ITGOverlayViewSwiftUI<AnyView>(
            channelSlug: channelSlug,
            accountId: accountId,
            environment: env,
            videoView: AnyView(playerViewModel.videoView.focused($focusedItem, equals: FocusableItem.player)),
            blockAll: blockItg,
            playerIsPlaying: playerViewModel.isPlaying,
            onOverlayRequestedVideoTime: {
                return playerViewModel.avplayer.currentTime().seconds
            },
            onOverlayRequestedPause: { playerViewModel.avplayer.pause() },
            onOverlayRequestedPlay: { playerViewModel.avplayer.play() },
            onOverlayRequestedFocus: { focusedItem = .itgOverlay },
            onOnOverlayReleasedFocus: { focusedItem = .player },
            onOverlayRequestedVideoSeek: playerViewModel.seekTo(_:),
            onOverlayRequestedVideoResolution: { playerViewModel.avplayer.currentItem?.presentationSize ?? .zero },
            onOverlayRequestedVideoLength: { playerViewModel.avplayer.currentItem?.duration.seconds ?? 0 },
            onOverlayRequestedVideoSoundLevel: { volume in playerViewModel.avplayer.volume = volume },
            onOverlayRequestedResetVideoSoundLevel: { playerViewModel.avplayer.volume = 1 }
        )
        .focused($focusedItem, equals: FocusableItem.itgOverlay)
        .onAppear {
            playerViewModel.avplayer.play()
            focusedItem = .player
        }
        .ignoresSafeArea()
    }
}
