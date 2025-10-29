package io.inthegame.awsdemo

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
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
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import com.amazon.mediatailorsdk.AdData
import com.amazon.mediatailorsdk.AdObserver
import com.amazon.mediatailorsdk.NonLinearAdsData
import com.syncedapps.inthegametv.ITGContent
import com.syncedapps.inthegametv.data.CloseOption
import com.syncedapps.inthegametv.domain.model.AnalyticsEventSnapshot
import com.syncedapps.inthegametv.domain.model.UserSnapshot
import com.syncedapps.inthegametv.integration.ITGExoLeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGMedia3LeanbackPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import io.datazoom.sdk.Datazoom
import io.datazoom.sdk.DzAdapter
import io.datazoom.sdk.media3.createContext
import io.datazoom.sdk.mediatailor.removeSession
import io.datazoom.sdk.mediatailor.setupAdSession
import io.datazoom.sdk.utils.AdapterCallbackListener
import io.datazoom.sdk.utils.DzPlaybackEvent
import io.inthegame.awsdemo.mediatailor.AwsMediaTailorController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@OptIn(UnstableApi::class)
class PlaybackVideoFragment : VideoSupportFragment() {

    private var datazoomAdapter: DzAdapter? = null
    private var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlayer: ExoPlayer? = null
    private var mITGComponent: ITGLeanbackComponent? = null
    private var mITGPlayerAdapter: ITGMedia3LeanbackPlayerAdapter? = null
    private var shouldNotShowControls = false
    private var currentItem: Int = 0
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true
//    private val awsMediaTailorController : AwsMediaTailorController by lazy { AwsMediaTailorController(
//        lifecycleScope,
//        { videoUrl ->
//            lifecycleScope.launch {
//                prepareMediaForPlaying(Uri.parse(videoUrl))
//                mPlayer?.playWhenReady = playWhenReady
//                mPlayer?.seekTo(currentItem, playbackPosition)
//                mPlayer?.prepare()
//            }
//        },
//        { flexiJson ->
//            lifecycleScope.launch {
//                if (mITGComponent?.itgOverlayView?.currentContent()
//                        ?.contains(ITGContent.FLEXI) == false
//                ) {
//                    aiConsoleManager?.startDisplayAdFlow()
//                    delay(1_500L)
//                    mITGComponent?.itgOverlayView?.injectFlexi(flexiJson)
//                }
//            }
//        }
//    ) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt("currentItem")
            playbackPosition = savedInstanceState.getLong("playbackPosition", 0L)
            playWhenReady = savedInstanceState.getBoolean("playWhenReady")
        }
    }

    private var aiConsoleManager: AIConsoleManager? = null

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
            "668d16a5cbbeb699b8dd9bcb",
            "test-channel",
            userBroadcasterForeignID = "android_${Date().time}",
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

        aiConsoleManager = AIConsoleManager(requireContext(), rootFrame, lifecycleScope)

        MediaTailorExampleHelper.implementMediaTailor(contentUrl = CONTENT_URL) { session, _, contentUrl ->
            session?.let {
                datazoomAdapter?.setupAdSession(session, surfaceView, contentUrl)

                session.addAdObserver(object  : AdObserver() {
                    override fun onNewNonLinearAds(adData: NonLinearAdsData) {
                        super.onNewNonLinearAds(adData)
                        Log.d("DATAZOOM", "onNewNonLinearAds $adData")
                    }

                    override fun onNonLinearAdsStart(
                        adData: NonLinearAdsData,
                        adElapsedTime: Double,
                        playhead: Double
                    ) {
                        super.onNonLinearAdsStart(adData, adElapsedTime, playhead)
                        Log.d("DATAZOOM", "onNonLinearAdsStart $adData")
                    }

                    override fun onNonLinearAdsEnd(adData: NonLinearAdsData) {
                        super.onNonLinearAdsEnd(adData)
                        Log.d("DATAZOOM", "onNonLinearAdsEnd $adData")
                    }

                    override fun onNewAd(adData: AdData, isReplacement: Boolean) {
                        super.onNewAd(adData, isReplacement)
                        Log.d("DATAZOOM", "onNewAd $adData isReplacement $isReplacement")
                    }
                })

                lifecycleScope.launch {
                    delay(1000)

                    mPlayer?.addMediaItem(
                        MediaItem.Builder().setUri(session.playbackUrl).build()
                    )
                    mPlayer?.playWhenReady = playWhenReady
                    mPlayer?.seekTo(currentItem, playbackPosition)
                    mPlayer?.prepare()
                    mPlayer?.play()
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            hideControlsOverlay(false)
        }, 30)
    }

    /** Pauses the player.  */
    @TargetApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue?.isPlaying == true) {
            mPlayerGlue?.pause()
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
        super.onStop()
    }

    private fun initializePlayer() {
        val player =
            ExoPlayer.Builder(requireContext(), DefaultRenderersFactory(requireContext())).build()
        mPlayer = player
        mPlayer?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    lifecycleScope.launch {
                        delay(1_000L)
                        aiConsoleManager?.startConsoleFlow()
                    }
                }
            }
        })
        datazoomAdapter = Datazoom.createContext(exoPlayer = player)
        datazoomAdapter?.addPlaybackListener(object : AdapterCallbackListener {
            override fun onAdPlaybackEvents(event: DzPlaybackEvent) {
                Log.d("DATAZOOM", "onAdPlaybackEvents $event")
            }

            override fun onPlaybackUpdate(playHeadMs: Long, livePlayHeadMs: Double?) {
            }

        })
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
        datazoomAdapter?.id?.let {
            Datazoom.removeContext(id = it)
        }
        datazoomAdapter?.removeSession()

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

    inner class ITGLeanbackComponent : ITGPlaybackComponent {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        override fun channelInfoDidLoad(streamUrl: String?) {
            super.channelInfoDidLoad(streamUrl)
        }

        //optional
        override fun overlayProducedAnalyticsEvent(eventSnapshot: AnalyticsEventSnapshot) {
            Log.d(
                this.javaClass.simpleName,
                "overlayProducedAnalyticsEvent eventSnapshot $eventSnapshot"
            )
        }

        //optional
        override fun userState(userSnapshot: UserSnapshot) {
            Log.d(this.javaClass.simpleName, "overlayUserUpdated userSnapshot $userSnapshot")
        }

        override fun overlayDidEndPresentingContent(content: ITGContent) {
            super.overlayDidEndPresentingContent(content)
            aiConsoleManager?.startConsoleFlow()
        }

        override fun overlayRequestedPlay() {
            shouldNotShowControls = true
            super.overlayRequestedPlay()
        }

        override fun overlayRequestedPause() {
            shouldNotShowControls = true
            super.overlayRequestedPlay()
        }

        override fun overlayRequestedSeekTo(timestampMillis: Long) {
            shouldNotShowControls = true
            super.overlayRequestedSeekTo(timestampMillis)
        }

        override fun overlayRequestedFocus(focusView: View) {
            Log.d(this.javaClass.simpleName, "overlayRequestedFocus focusView=$focusView")
        }

        override fun overlayReleasedFocus() {
            Log.d(this.javaClass.simpleName, "overlayReleasedFocus")
        }

        override fun overlayDidShowSidebar() {}

        override fun overlayDidHideSidebar() {}

        override fun overlayClickedUserArea() {
            Log.d("ITG", "CLICKED USER AREA")
        }

        override fun overlayClosedByUser(type: CloseOption, timestamp: Long) {
            Log.d("ITG", "ITG CLOSED - ${type.name}")
        }

    }

    companion object {
        private const val UPDATE_DELAY = 16
//        private const val CONTENT_URL = "https://1fd3d978cae34bfb8203bd5feea44953.mediatailor.us-east-1.amazonaws.com/v1/session/b4af9cd7f590baef44a681686a25208ee900a7a5/datazoom_mt_config/hls.m3u8"
//        private const val CONTENT_URL = "https://d2jzd9l24jb7u.cloudfront.net/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/NAB-ITG-SSAI_EMT-CDK/out/v1/4cc3b6168dee4c5caa47a3664f79ed27/index.m3u8"
        const val BASE_URL = "https://3e763f5a2cb64a869ea9bb83d5f933d7.mediatailor.us-west-2.amazonaws.com"
        private const val CONTENT_URL = "${BASE_URL}/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/demo_page_for_client_testing/index.m3u8"    }
}
