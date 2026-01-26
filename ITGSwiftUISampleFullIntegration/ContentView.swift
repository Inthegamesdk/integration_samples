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

class ITGPlayerViewModel: ObservableObject {
    
    @Published var isPlaying: Bool = false
    @Published var contentMode: ContentMode
    @Published var videoRect: CGRect = UIScreen().bounds
    let avplayer: AVPlayer
    var seekTimer: Timer? = nil
    var observer: NSKeyValueObservation?
    var itgOverlayView: ITGOverlayView? = nil
    
    init(_ videoUrl: URL) {
        self.avplayer = AVPlayer(url: videoUrl)
        self.contentMode = .fit
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
    
    @State var blockItg: Bool = false {
        didSet {
            playerViewModel.itgOverlayView?.block(blockItg)
        }
    }
    @StateObject var playerViewModel = ITGPlayerViewModel(URL(string: "https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4")!)
    @State var channelSlug: String = "demo"
    @State var accountId: String = "69230d1b5f7b3515524dd184"
    @State var env = ITGEnvironment(envName: "v2-7")
    @State private var containerRect: CGRect = .zero
    @FocusState private var focusedItem: FocusableItem?
    
    var body: some View {
        ZStack {
            GeometryReader { geometry in
                Color.clear
                    .onAppear {
                        containerRect = CGRect(origin: CGPoint.zero, size: geometry.size)
                        playerViewModel.videoRect = CGRect(origin: CGPoint.zero, size: geometry.size)
                    }
            }
            .ignoresSafeArea()
            VideoPlayer(player: playerViewModel.avplayer)
                .aspectRatio(contentMode: playerViewModel.contentMode)
                .frame(width: playerViewModel.videoRect.size.width, height: playerViewModel.videoRect.size.height)
                .position(x: playerViewModel.videoRect.midX, y: playerViewModel.videoRect.midY)
                .ignoresSafeArea();
            ITGOverlayViewSwiftUI<AnyView>(
                channelSlug: channelSlug,
                accountId: accountId,
                environment: env,
                showLogs: true,
                onItgDidLoadChannelInfo: nil,
                onItgRequestedVideoStateChange: { state, time in
                    if let time {
                        playerViewModel.seekTo(time)
                    }
                    if state == .playing {
                        playerViewModel.avplayer.play()
                    } else {
                        playerViewModel.avplayer.pause()
                    }
                },
                onItgRequestedFocusUpdate: { requiresFocus in
                    if requiresFocus {
                        focusedItem = .itgOverlay
                    } else {
                        focusedItem = .player
                    }
                },
                onItgRequestedVideoRectChange: { rect, time in
                    withAnimation(.easeInOut(duration: time)) {
                        playerViewModel.videoRect = rect ?? containerRect
                    }
                },
                onItgReceivedDeeplink: { deepLink in
                    print(deepLink)
                },
                onItgDidProcessAnalyticEvent: nil,
                onItgDidUpdateUserState: nil,
                onItgRequestedVideoSoundLevel: { sound in
                    playerViewModel.avplayer.volume = sound ?? 1
                },
                onItgRequestedVideoGravity: { videoGravity in
                    if videoGravity == .resize {
                        playerViewModel.contentMode = .fill
                    } else {
                        playerViewModel.contentMode = .fit
                    }
                },
                onItgOverlayCreated: { overlayView in
                    playerViewModel.itgOverlayView = overlayView
                })
            .focused($focusedItem, equals: FocusableItem.itgOverlay)
            .onAppear {
                playerViewModel.avplayer.play()
                focusedItem = .player
            }
            .onChange(of: playerViewModel.isPlaying, {
                let state = ITGVideoState(videoDuration: playerViewModel.avplayer.currentItem?.duration.seconds ?? 0,
                                          videoTime: playerViewModel.avplayer.currentTime().seconds,
                                          videoStatus: playerViewModel.isPlaying ? .playing : .paused,
                                          visibleContent: .content)
                playerViewModel.itgOverlayView?.playerChangedState(state)
            })
            .ignoresSafeArea()
        }
        .ignoresSafeArea()
    }
}
