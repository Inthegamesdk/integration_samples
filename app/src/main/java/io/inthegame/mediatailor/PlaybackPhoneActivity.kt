package io.inthegame.mediatailor


import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import android.view.ViewGroup
import java.util.*

import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.network.ITGEnvironment
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import io.inthegame.mediatailor.databinding.ActivityPhonePlaybackBinding
import io.inthegame.mediatailor.domain.useCase.basic.Resource.Companion.asSuccessful
import io.inthegame.mediatailor.helpers.FetchConfig

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private var videoView: PlayerView? = null

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3PlayerAdapter? = null

    private var itgMediaTailorPlugin: ITGMediaTailorPlugin? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        //add video view
        videoView = buildVideoView()

        //ITG  initialize the ITGPlaybackComponent
        initITG(savedInstanceState)

        lifecycleScope.launch {
            val mediatailorConfig =
                FetchConfig().invoke(
                    FetchConfig.Param(CONTENT_URL)
                ).asSuccessful() ?: return@launch

            startVideo(mediatailorConfig.manifestUrl.orEmpty())

            //ITG Mediatailor: start pooling
            startMediaTailor(mediatailorConfig.trackingUrl.orEmpty())
        }
    }

    private fun startMediaTailor(trackingUrl: String) {
        val plugin = ITGMediaTailorPlugin()

        // Required: overlay view used to schedule Flexis
        plugin.delegate = mITGComponent?.itgOverlayView

        // Optional: receive raw tracking data JSON
        plugin.listener = object : ITGMediaTailorPlugin.ITGMediaTailorListener {
            override fun didReceiveTrackingData(json: String) {
                Log.d(
                    this@PlaybackPhoneActivity.javaClass.simpleName,
                    "didReceiveTrackingData $json"
                )
            }
        }

        plugin.startMediaTailor(
            trackingURL = trackingUrl,
            interval = 5_000L
        )

        itgMediaTailorPlugin = plugin
    }

    private fun initITG(savedInstanceState: Bundle?) {
        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "69230d1b5f7b3515524dd184"
        val channelSlug = "demo_mediatailor"


        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(this)


        // Set up the ITGMedia3PlayerAdapter with your player view
        val adapter = ITGMedia3PlayerAdapter(playerView = videoView)
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            activity = this, //mandatory: fragment activity instance
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component

            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
            enableLogs = true
        )


        // Add the ITG component to your view hierarchy
        binding.outerContainer.addView(mITGComponent, 0)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // ITG: make sure ITG does not consume this back press
                if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                    // Implement your own back press action here
                    finish()
                }
            }
        })
    }

    private fun restorePlaybackStateIfAny(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }
    }

    private fun setupFullscreenMode() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun startVideo(url: String) {
        prepareMediaForPlaying(url.toUri())
        player?.playWhenReady = playWhenReady
        player?.seekTo(0, playbackPosition)
        player?.prepare()
    }

    @OptIn(UnstableApi::class)
    private fun prepareMediaForPlaying(mediaSourceUri: Uri) {
        val upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        val defaultDataSourceFactory =
            DefaultDataSource.Factory(this, upstreamDataSourceFactory)

        defaultDataSourceFactory.createDataSource()

        val mediaSource: MediaSource =
            if (mediaSourceUri.lastPathSegment?.endsWith(".m3u8") == true) {
                HlsMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(mediaSourceUri)
                    )
            } else {
                ProgressiveMediaSource.Factory(defaultDataSourceFactory)
                    .createMediaSource(
                        MediaItem.fromUri(mediaSourceUri)
                    )
            }
        player?.setMediaSource(mediaSource)
    }

    @SuppressLint("InflateParams")
    private fun buildVideoView(): PlayerView {
        val videoView =
            layoutInflater.inflate(R.layout.styled_player_view, null, false) as PlayerView
        videoView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return videoView
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(SEEK_INCREMENT)
            .setSeekForwardIncrementMs(SEEK_INCREMENT)
            .build()

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        videoView?.player = player
        this.player = player
    }

    private fun releasePlayer() {
        Log.d(this.javaClass.simpleName, "releasePlayer")
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            playWhenReady = exoPlayer.playWhenReady
            videoView?.player = null
            exoPlayer.release()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

        }
        player = null
    }

    override fun onResume() {
        super.onResume()
        if ((player == null)) {
            initializePlayer()
        }
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    public override fun onPause() {
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)

        // Saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.dispatchKeyEvent(event)
        // ... rest of your dispatchKeyEvent code
        return super.dispatchKeyEvent(event)
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyUp(keyCode, event)
        // ... rest of your onKeyUp code
        return super.onKeyUp(keyCode, event)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyDown(keyCode, event)
        // ... rest of your onKeyDown code
        return super.onKeyDown(keyCode, event)
    }


    companion object {
        private const val SEEK_INCREMENT = 10_000L
        private const val CONTENT_URL =
            "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/"
    }
}