import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class AVPlayer extends StatelessWidget{
  const AVPlayer({super.key, required this.videoUrl});
  static const _appleViewType = "avplayer_view";

  final String videoUrl;

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: 16 / 9,
      child: UiKitView(
          viewType: _appleViewType,
          creationParams: <String, dynamic>{
            'videoUrl': videoUrl,
            },
          creationParamsCodec: const StandardMessageCodec()
      ),
    );
  }

}