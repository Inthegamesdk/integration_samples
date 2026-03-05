package io.inthegame.datazoom


import android.annotation.SuppressLint
import android.content.res.Configuration
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
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.amazon.mediatailorsdk.AdObserver
import com.amazon.mediatailorsdk.Session
import com.syncedapps.inthegametv.utils.DeviceUtils.isMobile
import io.datazoom.sdk.Config.Builder
import io.datazoom.sdk.Datazoom
import io.datazoom.sdk.DzAdapter
import io.datazoom.sdk.logs.LogLevel
import io.datazoom.sdk.media3.createContext
import io.datazoom.sdk.mediatailor.setupAdSession
import io.datazoom.sdk.utils.or
import io.inthegame.datazoom.ITGDatazoomPlugin.attachITG
import io.inthegame.datazoom.databinding.ActivityPhonePlaybackBinding

class PlaybackPhoneActivity : FragmentActivity() {

    private var datazoomAdapter: DzAdapter? = null

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var player: ExoPlayer? = null
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private lateinit var videoView: PlayerView

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3PlayerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configId = "f5562a7c-9e5f-4c60-b7c4-d174808c5d38"

        if (configId.isEmpty() || configId == "{DATAZOOM_CONFIG_ID}"){
            throw IllegalArgumentException("Please provide your Datazoom configId")
        }

        Datazoom.init(
            Builder(configId)
                .logLevel(LogLevel.VERBOSE)
                .build()
        )

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        applyVideoConstraints(binding.outerContainer)

        videoView = buildVideoView()

        //ITG  initialize the ITGPlaybackComponent
        initITG(savedInstanceState)
        initMediaTailor()
    }

    // Datazoom start
    private var itgAdObserver : AdObserver? = null
    private var session : Session? = null

    private fun initMediaTailor() {
        MediaTailorExampleHelper.implementMediaTailor(contentUrl = CONTENT_URL) { session, _, contentUrl ->
            session?.let {
                this.session = session
                datazoomAdapter?.setupAdSession(session, videoView, contentUrl)

                // Attach ITG to Datazoom
                mITGComponent?.let { itg ->
                    itgAdObserver = session.attachITG(itg)
                }

                startVideo(session.playbackUrl.orEmpty())
            }
        }
    }
    // Datazoom end

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

        // Datazoom start
        datazoomAdapter = Datazoom.createContext(exoPlayer = player)
        // Datazoom end

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isMobile())
            applyVideoConstraints(binding.outerContainer)
    }


    private fun applyVideoConstraints(videoView: View) {
        val lp = videoView.layoutParams as ConstraintLayout.LayoutParams

        val isLandscape =
            videoView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // Fill the parent (remove ratio, constrain to all sides)
            lp.dimensionRatio = null
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

            // Usually: match constraints on both axes
            lp.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        } else {
            // Portrait: keep 16:9 ratio and top-aligned (no bottom constraint)
            lp.dimensionRatio = "H,16:9"
            lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            lp.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID

            // Width constrained by start/end, height computed from ratio
            lp.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        }

        videoView.layoutParams = lp
        videoView.requestLayout()
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
            "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/index.m3u8"
    }
}