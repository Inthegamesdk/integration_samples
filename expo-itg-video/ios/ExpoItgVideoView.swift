import ExpoModulesCore
import AVKit
import ITGPlayerViewController
#if os(iOS)
import InthegametviOS
#else
import Inthegametv
#endif

public class ExpoItgVideoView: ExpoView {
    
    var itgPlayerController: ITGPlayerViewController!
    let mediaUrl = "https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4"
    
    required init(appContext: AppContext? = nil) {
        super.init(appContext: appContext)
        
        let player = AVPlayer(url: URL(string: mediaUrl)!)
        let playerViewController = AVPlayerViewController()
        playerViewController.player = player
        
        let playerAdapter = ITGAVPlayerAdapter(player, playerViewController: playerViewController)
        itgPlayerController = ITGPlayerViewController(channelSlug: "samplechannel", accountId: "68650da0324217d506bcc2d4", environment: ITGEnvironment(envName: "v2-3"), playerAdapter: playerAdapter)
        itgPlayerController.shouldPlayChannelVideo = false
#if os(iOS)
        itgPlayerController.closeButtonVisibilityMode = .hidden
#endif
        addSubview(itgPlayerController.view)
        itgPlayerController.view.constraintsFillSuperview()
        player.play()
        
    }
    
    public override func layoutSubviews() {
        itgPlayerController.view.frame = bounds
    }
    
}
