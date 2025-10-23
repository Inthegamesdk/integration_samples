import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'exo_player.dart';
class ITGNativeVideoPlayer extends StatelessWidget{
  const ITGNativeVideoPlayer({super.key, required this.videoUrl});

  final String videoUrl;

  final methodChannel = const MethodChannel('flutter_itg_native_video_player');

  Future<bool> _handleBackPress() async {
    final consumed =
        await methodChannel.invokeMethod<bool>('onBackPressed') ?? false;
    return !consumed;
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
        canPop: false,
        onPopInvokedWithResult: (didPop, _) async {
          if (didPop) return;
          final navigator = Navigator.of(context);
          final shouldPop = await _handleBackPress();
          if (shouldPop) {
            if (navigator.canPop()) {
              navigator.maybePop();
            } else {
              SystemNavigator.pop();
            }
          }
        },
        child: defaultTargetPlatform == TargetPlatform.android
            ? AndroidExoPlayer(videoUrl: videoUrl)
            : throw UnsupportedError("Unsupported platform view"),
    );
  }

}
