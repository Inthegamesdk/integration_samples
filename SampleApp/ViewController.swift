//
//  ViewController.swift
//  TestApp
//
//  Created by ilya khymych on 25.03.2025.
//

import UIKit
import AVKit
import ItgPlayerViewController
import ItgMediatailorPlugin
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif

class ViewController: UIViewController {
    
    let mediaTailorSource = "https://d37963fd1a374034af5ccda89ee59e85.mediatailor.us-east-1.amazonaws.com/v1/session/dffceb859a31f14d0ed059ef6b4e2e4d850e60b1/livh_1091/1091_E_ENTERTAINMENT_HD_HLS/master.m3u8"
    let channelSlug: String = "demo_mediatailor"
    let accountId: String = "69230d1b5f7b3515524dd184"
    var itgPlayerController: ITGPlayerViewController!
    var mediatailorPlugin: ITGMediatailorPlugin!
        
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

        mediatailorPlugin = ITGMediatailorPlugin(dataDelegate: self, flexiDelegate: itgPlayerController.overlayView!)
        let url = URL(string: mediaTailorSource)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            if let data, let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any], let trackingUrl = json["trackingUrl"] as? String, let manifestUrl = json["manifestUrl"] as? String {
                DispatchQueue.main.async {
                    self?.mediatailorPlugin.startMediaTailor(url: "\(url.scheme!)://\(url.host!)" + trackingUrl, interval: 5, injectImmediately: true)
                    player.replaceCurrentItem(with: AVPlayerItem(url: URL(string: "\(url.scheme!)://\(url.host!)" + manifestUrl)!))
                    player.play()
                }
            }
        }.resume()
    }
    
}

extension ViewController: ITGMediatailorPluginDelegate {
    
    func didReceiveTrackingData(_ jsonData: [String : Any]) {
        print("Received: \(jsonData)")
    }
        
}
