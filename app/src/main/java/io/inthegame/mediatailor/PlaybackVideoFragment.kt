package io.inthegame.mediatailor

import android.content.Context
import android.graphics.Color
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
import com.syncedapps.inthegametv.integration.ITGMedia3LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametv.network.ITGEnvironment
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import io.inthegame.mediatailor.domain.useCase.basic.Resource.Companion.asSuccessful
import io.inthegame.mediatailor.helpers.FetchConfig

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

    private var itgMediaTailorPlugin: ITGMediaTailorPlugin? = null


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
            val mediatailorConfig =
                FetchConfig().invoke(
                    FetchConfig.Param(CONTENT_URL)
                ).asSuccessful() ?: return@launch

            startPlayback(mediatailorConfig.manifestUrl)

            //ITG Mediatailor: start pooling
            startMediaTailor(mediatailorConfig.trackingUrl.orEmpty())
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

    private fun initITG(savedInstanceState: Bundle?) {
        val accountId = "69230d1b5f7b3515524dd184"
        val channelSlug = "demo_mediatailor"

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
            accountId,
            channelSlug,
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

    private fun startMediaTailor(trackingUrl: String) {
        val plugin = ITGMediaTailorPlugin()

        // Required: overlay view used to schedule Flexis
        plugin.delegate = mITGComponent?.itgOverlayView

        // Optional: receive raw tracking data JSON
        plugin.listener = object : ITGMediaTailorPlugin.ITGMediaTailorListener {
            override fun didReceiveTrackingData(json: String) {
                Log.d(
                    this@PlaybackVideoFragment.javaClass.simpleName,
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
                MediaItem.Builder().setUri(videoUrl.toUri()).build()
            )
            mPlayer?.playWhenReady = playWhenReady
            mPlayer?.seekTo(currentItem, playbackPosition)
            mPlayer?.prepare()
        }
    }

    fun handleBackPressIfNeeded(): Boolean {
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

    class ITGLeanbackComponent : ITGPlaybackComponent {

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
            "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/"

    }
}
