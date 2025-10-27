import Flutter
import ITGPlayerViewController
import UIKit
import AVKit

class FLNativeViewFactory: NSObject, FlutterPlatformViewFactory {
    private var messenger: FlutterBinaryMessenger

    init(messenger: FlutterBinaryMessenger) {
        self.messenger = messenger
        super.init()
    }

    func create(
        withFrame frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?
    ) -> FlutterPlatformView {
        return FLNativeView(
            frame: frame,
            viewIdentifier: viewId,
            arguments: args,
            binaryMessenger: messenger)
    }

    /// Implementing this method is only necessary when the `arguments` in `createWithFrame` is not `nil`.
    public func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
          return FlutterStandardMessageCodec.sharedInstance()
    }
}

class FLNativeView: NSObject, FlutterPlatformView {
    private var _view: UIView
    private var playerViewController: ITGPlayerViewController!
    
    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?
    ) {
        _view = UIView()
        _view.backgroundColor = UIColor.clear
        super.init()
        let videoUrl = (args as? [String: Any]?)??["videoUrl"] as? String ?? ""
        let playerAdapter = ITGAVPlayerAdapter(AVPlayer(), playerViewController: AVPlayerViewController())
        playerViewController = ITGPlayerViewController(channelSlug: "samplechannel", accountId: "68650da0324217d506bcc2d4", playerAdapter: playerAdapter)
        playerViewController.shouldPlayChannelVideo = false
        _view.addSubview(playerViewController.view)
        playerViewController.view.constraintsFillSuperview()
        if let url = URL(string: videoUrl) {
            playerViewController.startVideo(url)
        }
    }

    func view() -> UIView {
        return _view
    }

}
