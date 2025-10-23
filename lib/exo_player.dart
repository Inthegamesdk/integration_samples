import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AndroidExoPlayer extends StatelessWidget{
  const AndroidExoPlayer({super.key, required this.videoUrl});
  static const _androidViewType = "exoplayer_view";

  final String videoUrl;

  @override
  Widget build(BuildContext context) {
    return AspectRatio(
      aspectRatio: 16 / 9,
      child: AndroidView(
          viewType: _androidViewType,
          creationParams: <String, dynamic>{
            'videoUrl': videoUrl,
            },
          creationParamsCodec: const StandardMessageCodec()
      ),
    );
  }

}