//
//  ViewController.swift
//  TestApp
//
//  Created by ilya khymych on 25.03.2025.
//

import UIKit
import AVKit
import ITGPlayerViewController
import ITGMediatailorPlugin
#if os(tvOS)
import Inthegametv
#else
import InthegametviOS
#endif

class ViewController: UIViewController {
    
    let mediaTailorSource = "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/index.m3u8"
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

        mediatailorPlugin = ITGMediatailorPlugin.init(dataDelegate: self, flexiDelegate: itgPlayerController.overlayView!)
        let url = URL(string: mediaTailorSource)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            if let data, let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any], let trackingUrl = json["trackingUrl"] as? String, let manifestUrl = json["manifestUrl"] as? String {
                DispatchQueue.main.async {
                    self?.mediatailorPlugin.startMediaTailor(url: "\(url.scheme!)://\(url.host!)" + trackingUrl, interval: 5)
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
