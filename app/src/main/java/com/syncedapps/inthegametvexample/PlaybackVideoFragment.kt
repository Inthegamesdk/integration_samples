package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGMedia3LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametvexample.mediatailor.FetchConfig
import io.inthegame.mediatailor.ITGMediaTailorPlugin
import io.inthegame.mediatailor.domain.useCase.basic.Resource.Companion.asSuccessful
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.syncedapps.inthegametvexample.Const.MEDITAILOR_URL

class PlaybackVideoFragment : VideoSupportFragment() {

    @SuppressLint("UnsafeOptInUsageError")
    private var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null

    @SuppressLint("UnsafeOptInUsageError")
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var shouldNotShowControls = false

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3LeanbackPlayerAdapter? = null
    private var mITGMediaTailorPlugin: ITGMediaTailorPlugin? = null


    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        initITG(savedInstanceState)
        initMediaTailor()
    }

    private fun initITG(savedInstanceState: Bundle?) {
        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "69230d1b5f7b3515524dd184"
        val channelSlug = "demo_mediatailor"


        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(requireContext())


        // Set up the ITGExoLeanbackPlayerAdapter with your player view
        val adapter = ITGMedia3LeanbackPlayerAdapter(playerView = surfaceView)
        mITGPlayerAdapter = adapter


        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            root = requireView(), //mandatory: root view of the screen
            lifecycleOwner = viewLifecycleOwner, //mandatory: the view's lifecycle owner
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component


            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
        )


        // Add the ITG component to your view hierarchy
        (requireView() as ViewGroup).addView(mITGComponent, 0)


        //Setup backpress listener

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                        // Implement your own back press action here
                        requireActivity().finish()
                    }
                }
            })
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initMediaTailor() = lifecycleScope.launch {
        // init MT
        val config =
            FetchConfig().invoke(
                FetchConfig.Param(MEDITAILOR_URL)
            ).asSuccessful()

        // init ITG MT Plugin
        val itgMediaTailorPlugin = ITGMediaTailorPlugin()
        itgMediaTailorPlugin.delegate = mITGComponent?.itgOverlayView ?: return@launch

        // assign optional listener
        itgMediaTailorPlugin.listener =
            object : ITGMediaTailorPlugin.ITGMediaTailorListener {
                override fun didReceiveTrackingData(json: String) {
                    Log.d(
                        this@PlaybackVideoFragment.javaClass.simpleName,
                        "didReceiveTrackingData $json"
                    )
                }
            }

        // start the plugin
        itgMediaTailorPlugin.startMediaTailor(
            config?.trackingUrl.orEmpty(),
            5_000L,
            injectImmediately = true
        )

        mITGMediaTailorPlugin = itgMediaTailorPlugin

        // start playback
        config?.manifestUrl?.let { play(it) }
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    @OptIn(UnstableApi::class)
    override fun onResume() {
        super.onResume()
        if (mPlayer == null) {
            initializePlayer()
        }
    }

    /** Pauses the player.  */
    @OptIn(UnstableApi::class)
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue?.isPlaying == true) {
            mPlayerGlue?.pause()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }


    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player

        // Notify the ITGPlayerAdapter that the player is ready
        mITGPlayerAdapter?.onPlayerReady(player)

        mPlayerAdapter = LeanbackPlayerAdapter(requireContext(), player, UPDATE_DELAY)
        mPlayerGlue = PlaybackTransportControlGlue(requireActivity(), mPlayerAdapter)
        mPlayerGlue?.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue?.playWhenPrepared()
        isControlsOverlayAutoHideEnabled = true
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer?.release()

            // Notify the ITGPlayerAdapter that the player has been released
            mITGPlayerAdapter?.onPlayerReleased()

            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    @UnstableApi
    private fun play(streamUrl: String) {
        mPlayer?.setMediaItem(MediaItem.fromUri(streamUrl))
        mPlayerGlue?.play()
    }

    override fun showControlsOverlay(runAnimation: Boolean) {
        if (shouldNotShowControls) {
            shouldNotShowControls = false
        } else {
            super.showControlsOverlay(runAnimation)
        }
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
        private const val UPDATE_DELAY = 16
    }
}
