package io.inthegame.awsdemo

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import com.syncedapps.inthegametv.ITGContent
import com.syncedapps.inthegametv.domain.model.AnalyticsEventSnapshot
import com.syncedapps.inthegametv.domain.model.UserSnapshot
import com.syncedapps.inthegametv.integration.ITGMedia3LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametv.network.ITGEnvironment
import io.inthegame.awsdemo.mediatailor.AwsMediaTailorController
import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(UnstableApi::class)
class PlaybackVideoFragment : VideoSupportFragment() {

    private var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var mITGComponent: ITGLeanbackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3LeanbackPlayerAdapter? = null
    private var shouldNotShowControls = false
    private var currentItem: Int = 0
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
    private val awsMediaTailorController: AwsMediaTailorController by lazy {
        AwsMediaTailorController(
            lifecycleScope,
            this::startPlayback
        ) { flexiJson ->
            mITGComponent?.itgOverlayView?.injectFlexi(flexiJson)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt("currentItem")
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.BLACK)
        initITG(savedInstanceState)
        lifecycleScope.launch {
            awsMediaTailorController.initialize(CONTENT_URL)
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (mPlayer == null) {
            initializePlayer()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            hideControlsOverlay(false)
        }, 30)
    }

    /** Pauses the player.  */
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue?.isPlaying == true) {
            mPlayerGlue?.pause()
        }
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    private fun initITG(savedInstanceState : Bundle?) {
        // create the overlay
        val adapter = ITGMedia3LeanbackPlayerAdapter(
            playerView = surfaceView
        )
        mITGPlayerAdapter = adapter
        mITGComponent = ITGLeanbackComponent(requireContext())
        mITGComponent?.init(
            requireView(),
            viewLifecycleOwner,
            adapter,
            "62a73d850bcf95e08a025f82",
            "demo",
            itgEnvironment = ITGEnvironment.dev,
            savedState = savedInstanceState
        )
        (requireView() as ViewGroup).addView(mITGComponent, 0)

        val rootFrame = FrameLayout(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        (requireView() as ViewGroup).addView(rootFrame, 3)
    }

    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player
        mITGPlayerAdapter?.onPlayerReady(player)
        mPlayerAdapter = LeanbackPlayerAdapter(requireContext(), player, UPDATE_DELAY)
        mPlayerGlue = PlaybackTransportControlGlue(
            activity,
            mPlayerAdapter,
        )
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true
    }

    private fun releasePlayer() {
        mPlayer?.let { player ->
            playbackPosition = player.currentPosition
            currentItem = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            mITGPlayerAdapter?.onPlayerReleased()
            mPlayer?.release()
            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    private fun startPlayback(videoUrl: String?) {
        Log.d(this.javaClass.simpleName, "playVideo $videoUrl")
        if (videoUrl.isNullOrEmpty()) return
        lifecycleScope.launch {
            mPlayer?.addMediaItem(
                MediaItem.Builder().setUri(Uri.parse(videoUrl)).build()
            )
            mPlayer?.playWhenReady = playWhenReady
            mPlayer?.seekTo(currentItem, playbackPosition)
            mPlayer?.prepare()
        }
    }

    suspend fun handleBackPressIfNeeded(): Boolean {
        return mITGComponent?.handleBackPressIfNeeded() ?: false
    }

    override fun showControlsOverlay(runAnimation: Boolean) {
        if (shouldNotShowControls) {
            shouldNotShowControls = false
        } else {
            super.showControlsOverlay(runAnimation)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mITGComponent?.onSaveInstanceState(outState)
        outState.putInt("currentItem", currentItem)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)
    }

    inner class ITGLeanbackComponent : ITGPlaybackComponent {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )
    }

    companion object {
        private const val UPDATE_DELAY = 16
        private const val CONTENT_URL =
            "${ITGM3U8.BASE_URL}/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/demo_page_for_client_testing/index.m3u8"
    }
}
