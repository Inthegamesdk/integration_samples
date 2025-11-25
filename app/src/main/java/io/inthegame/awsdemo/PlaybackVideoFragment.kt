package io.inthegame.awsdemo

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import com.amazon.mediatailorsdk.AdObserver
import com.amazon.mediatailorsdk.Session
import com.syncedapps.inthegametv.integration.ITGMedia3LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametv.network.ITGEnvironment
import io.datazoom.sdk.Datazoom
import io.datazoom.sdk.DzAdapter
import io.datazoom.sdk.media3.createContext
import io.datazoom.sdk.mediatailor.removeSession
import io.datazoom.sdk.mediatailor.setupAdSession
import io.inthegame.datazoom.ITGDatazoomUtil.attachITG

@OptIn(UnstableApi::class)
class PlaybackVideoFragment : VideoSupportFragment() {

    private var datazoomAdapter: DzAdapter? = null
    private var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var mITGComponent: ITGLeanbackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3LeanbackPlayerAdapter? = null
    private var shouldNotShowControls = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.BLACK)
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
            "69230d1b5f7b3515524dd184",
            "demo",
            itgEnvironment = ITGEnvironment.dev,
            savedState = savedInstanceState
        )
        (requireView() as ViewGroup).addView(mITGComponent, 0)

        // Datazoom start
        initMediaTailor()
        // Datazoom end
    }

    // Datazoom start
    private var itgAdObserver : AdObserver? = null
    private var session : Session? = null
    private fun initMediaTailor() {
        MediaTailorExampleHelper.implementMediaTailor(contentUrl = CONTENT_URL) { session, _, contentUrl ->
            session?.let {
                this.session = session
                datazoomAdapter?.setupAdSession(session, surfaceView, contentUrl)

                // Attach ITG to Datazoom
                mITGComponent?.let { itg -> itgAdObserver = session.attachITG(itg) }

                startPlayback(session)
            }
        }
    }
    // Datazoom end

    private fun startPlayback(session : Session) {
        mPlayer?.addMediaItem(
            MediaItem.Builder().setUri(session.playbackUrl).build()
        )
        mPlayer?.prepare()
        mPlayer?.play()
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

    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player
        // Datazoom start
        datazoomAdapter = Datazoom.createContext(exoPlayer = player)
        // Datazoom end
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
        // Datazoom start
        itgAdObserver?.let { session?.removeAdObserver(it) }
        itgAdObserver = null
        session = null
        datazoomAdapter?.id?.let {
            Datazoom.removeContext(id = it)
        }
        datazoomAdapter?.removeSession()
        // Datazoom end

        mPlayer?.let {
            mITGPlayerAdapter?.onPlayerReleased()
            mPlayer?.release()
            mPlayer = null
            mPlayerGlue = null
            mPlayerAdapter = null
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
        private const val BASE_URL =
            "https://3e763f5a2cb64a869ea9bb83d5f933d7.mediatailor.us-west-2.amazonaws.com"
        private const val CONTENT_URL =
            "${BASE_URL}/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/demo_page_for_client_testing/index.m3u8"
    }
}
