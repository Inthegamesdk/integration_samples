package com.example.itg_example_app

import io.flutter.embedding.android.FlutterActivity
import kotlinx.coroutines.launch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.lifecycle.LifecycleOwner


class ITGExoPlayerViewFactory(
    private val context: Context,
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        return ITGExoPlayerView(this.context, messenger, args as? Map<*, *>)
    }
}

class ITGExoPlayerView(
    private val context: Context,
    messenger: BinaryMessenger,
    args: Map<*, *>?
) : PlatformView, LifecycleOwner {
    private val playerView: PlayerView = PlayerView(context)
    private val exoPlayer: ExoPlayer

    private val mITGComponent: ITGPlaybackComponent
    private val mITGPlayerAdapter: ITGMedia3PlayerAdapter

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        exoPlayer = ExoPlayer.Builder(context).build()
        playerView.player = exoPlayer

        val url = (args?.get("videoUrl") as? String).orEmpty()
        val mediaItem = MediaItem.fromUri(Uri.parse(url))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()

        // start ITG integration
        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(context)

        // Set up the ITGMedia3PlayerAdapter with your player view
        val adapter = ITGMedia3PlayerAdapter(playerView = playerView)
        mITGPlayerAdapter = adapter

        mITGPlayerAdapter.onPlayerReady(exoPlayer)

        val accountId = "68650da0324217d506bcc2d4"
        val channelSlug = "samplechannel"
        // Initialize the ITG component with necessary parameters
        mITGComponent.init(
            root = (context as FlutterActivity).findViewById(android.R.id.content),
            lifecycleOwner = this,
            playerAdapter = adapter, //mandatory: adapter between the player and SDK

            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
        )

        mITGComponent.setOnKeyListener { _, keyCode, event ->
            return@setOnKeyListener mITGComponent.itgOverlayView?.isKeyEventConsumable(event) == true
        }
        // end ITG integration

        MethodChannel(messenger, "flutter_itg_native_video_player")
            .setMethodCallHandler { call, result ->
                if (call.method == "onBackPressed") {
                    val consumed = mITGComponent.handleBackPressIfNeeded()
                    result.success(consumed)
                } else result.notImplemented()
            }
    }

    override fun getView(): View {
        return mITGComponent
    }

    override fun dispose() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        // release video player adapter
        mITGPlayerAdapter.onPlayerReleased()
        exoPlayer.release()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}

