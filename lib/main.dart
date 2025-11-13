import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // ⬅️ add this
import 'native_player.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Lock orientation to landscape only
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ITG Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const SafeArea(
        child: ITGNativeVideoPlayer(
          videoUrl:
          "https://assets.internal.inthegame.io/uploads/dev/testing/videos/fullvideonotext_1257glu3115.mp4",
        ),
      ),
    );
  }
}
