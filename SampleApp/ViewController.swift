//
//  ViewController.swift
//  TestApp
//
//  Created by ilya khymych on 25.03.2025.
//

import UIKit
import AVKit
import ItgPlayerViewController
import ItgDatazoomPlugin
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif
import DzMediaTailorAdapter
import MediaTailorSDK
import DzBase
import DzAVPlayerAdapter

class ViewController: UIViewController {
    
    let mediaTailorSource = "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/index.m3u8"
    let channelSlug: String = "demo_mediatailor"
    let accountId: String = "69230d1b5f7b3515524dd184"
    var itgPlayerController: ITGPlayerViewController!
    var datazoomPlugin: ITGDatazoomPlugin!
    private var session: Session? = nil
    private var dzAdapter: DzAdapter?
        
    @IBAction func openChannelAction(_ sender: Any) {
        
        let player = AVPlayer()
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        
        let playerAdapter = ITGAVPlayerAdapter(player, playerViewController: playerViewController)
        itgPlayerController = ITGPlayerViewController(channelSlug: channelSlug, accountId: accountId, environment: ITGEnvironment(envName: "v2-7"), playerAdapter: playerAdapter, showLogs: true)
        itgPlayerController.shouldPlayChannelVideo = false
#if os(iOS)
        itgPlayerController.closeButtonVisibilityMode = .hidden
#endif
        view.addSubview(itgPlayerController.view)
        itgPlayerController.view.constraintsFillSuperview()

        let dataZoomDataUrl = "https://a01be44a68244b86be45d41c3f637342.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosports27_test/index.m3u8"
        let configBuilder = Config.Builder(configurationId: "f5562a7c-9e5f-4c60-b7c4-d174808c5d38")
        Datazoom.shared.doInit(config: configBuilder.build())
        let config = SessionConfiguration.Builder().sessionInitUrl(value: dataZoomDataUrl).build()
        MediaTailor.shared.createSession(config: config, callback: { [weak self] session, error in
            if let session, self != nil {
                self?.session = session
                self?.datazoomPlugin = ITGDatazoomPlugin(flexiDelegate: self!.itgPlayerController.overlayView!)
                self?.session?.addAdObserver(adObserver: self!.datazoomPlugin)
                self?.dzAdapter = Datazoom.shared.createContext(player: playerAdapter.player)
                self?.dzAdapter?.configureAdSession(adSession: session, videoUrl: dataZoomDataUrl, videoPlayerView: playerAdapter.getPlayerView()!)
            }
            if let url = session?.playbackUrl {
                playerAdapter.startVideo(URL(string: url)!)
            }
        })
    }
    
}
