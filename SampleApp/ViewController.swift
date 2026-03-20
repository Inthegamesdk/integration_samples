//
//  ViewController.swift
//  TestApp
//
//  Created by ilya khymych on 25.03.2025.
//

import UIKit
import AVKit
import ItgPlayerViewController
import ItgGoogleIMAPlugin
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif

class ViewController: UIViewController {
    
    let mediaUrl = "https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4"
    let channelSlug: String = "demo"
    let accountId: String = "69230d1b5f7b3515524dd184"
    var itgPlayerController: ITGPlayerViewController!
        
    @IBAction func openChannelAction(_ sender: Any) {
      
        let player = AVPlayer(url: URL(string: mediaUrl)!)
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        
        let playerAdapter = ITGAVPlayerAdapter(player, playerViewController: playerViewController)
        itgPlayerController = ITGPlayerViewController(channelSlug: channelSlug, accountId: accountId, playerAdapter: playerAdapter)
        itgPlayerController.shouldPlayChannelVideo = false
#if os(iOS)
        itgPlayerController.closeButtonVisibilityMode = .hidden
#endif
        view.addSubview(itgPlayerController.view)
        itgPlayerController.view.constraintsFillSuperview()
        itgPlayerController.overlayView?.imaPlugin = ITGGoogleIMAPlugin()
        player.play()
        
    }
    
}

