package com.example.itg_example_app

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory("exoplayer_view", ITGExoPlayerViewFactory(this, flutterEngine.dartExecutor.binaryMessenger))
    }

}
