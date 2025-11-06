package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bitmovin.analytics.api.AnalyticsConfig
import com.bitmovin.analytics.api.SourceMetadata
import com.bitmovin.player.PlayerView
import com.bitmovin.player.api.Player
import com.bitmovin.player.api.analytics.AnalyticsPlayerConfig
import com.bitmovin.player.api.analytics.AnalyticsSourceConfig
import com.bitmovin.player.api.source.Source
import com.bitmovin.player.api.source.SourceConfig
import com.bitmovin.player.api.source.SourceType
import com.syncedapps.inthegametv.ITGOverlayView
import com.syncedapps.inthegametv.integration.ITGBitmovinPlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import kotlinx.coroutines.launch
import java.util.*

class PlaybackPhoneActivity : FragmentActivity() {

    private lateinit var binding: ActivityPhonePlaybackBinding
    private var playbackPosition: Long = 0L
    private var playWhenReady: Boolean = true

    private var mITGComponent: ITGPlaybackComponent? = null
    private var mITGPlayerAdapter: ITGBitmovinPlayerAdapter? = null

    private var analyticsLicenseKey = "fa295408-0e32-4aa9-867d-57cc40f4130d"
    private lateinit var playerView: PlayerView


    override fun onStart() {
        super.onStart()
        playerView.onStart()
    }

    override fun onResume() {
        super.onResume()
        playerView.onResume()
    }

    override fun onPause() {
        super.onPause()
        playerView.onPause()
    }

    override fun onStop() {
        super.onStop()
        playerView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        // ITG: release player
        mITGPlayerAdapter?.onPlayerReleased()
        playerView.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restorePlaybackStateIfAny(savedInstanceState)

        binding = ActivityPhonePlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupFullscreenMode()

        val analyticsConfig = AnalyticsConfig(
            licenseKey = analyticsLicenseKey,
        )
        val player = Player(
            context = this,
            analyticsConfig = AnalyticsPlayerConfig.Enabled(analyticsConfig),
        )
        playerView = binding.playerView
        playerView.player = player

        val streamTitle = "ITG BITMOVIN DEMO"
        val source = Source(
            SourceConfig(
                url = Const.VIDEO_URL,
                type = SourceType.Progressive,
                title = streamTitle,
            ),
            AnalyticsSourceConfig.Enabled(
                SourceMetadata(
                    videoId = "android-wizard-Art_of_Motion-1758794536553",
                    title = streamTitle,
                )
            ),
        )

        player.load(source)

        // ITG: init
        initITG(player, savedInstanceState)
    }

    private fun initITG(player: Player, savedInstanceState: Bundle?) {
        // Replace 'your_account_id' and 'your_channel_slug' with actual values
        val accountId = "62a73d850bcf95e08a025f82"
        val channelSlug = "demo"

        // Initialize ITGPlaybackComponent
        mITGComponent = ITGPlaybackComponent(this)

        // Set up the ITGMedia3PlayerAdapter with your player view
        val adapter = ITGBitmovinPlayerAdapter(playerView = playerView)
        mITGPlayerAdapter = adapter

        // provide ITG with the player instance
        adapter.onPlayerReady(player)

        // Initialize the ITG component with necessary parameters
        mITGComponent?.init(
            activity = this, //mandatory: fragment activity instance
            playerAdapter = adapter, //mandatory: adapter between the player and SDK
            savedState = savedInstanceState, //mandatory: saved state of the component

            accountId = accountId, //mandatory: your ITG accountId
            channelSlug = channelSlug, //mandatory: your channelId on our admin panel
        )

        // Add the ITG component to your view hierarchy
        binding.outerContainer.addView(mITGComponent, 0)

        // ITG: handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                lifecycleScope.launch {
                    // ITG: make sure ITG does not consume this back press
                    if (mITGComponent == null || mITGComponent?.handleBackPressIfNeeded() == false) {
                        // Implement your own back press action here
                        finish()
                    }
                }
            }
        })
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playbackPosition", playbackPosition)
        outState.putBoolean("playWhenReady", playWhenReady)

        // ITG: saving the state of the SDK
        mITGComponent?.onSaveInstanceState(outState)
    }

    // ITG: handle key events
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        // ITG: make sure ITG doesn't consume the event
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.dispatchKeyEvent(event)
        // ... rest of your dispatchKeyEvent code
        return super.dispatchKeyEvent(event)
    }


    // ITG: handle key events
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // ITG: make sure ITG doesn't consume the event
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyUp(keyCode, event)
        // ... rest of your onKeyUp code
        return super.onKeyUp(keyCode, event)
    }


    // ITG: handle key events
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // ITG: make sure ITG doesn't consume the event
        if (mITGComponent?.itgOverlayView?.isKeyEventConsumable(event) == true)
            return super.onKeyDown(keyCode, event)
        // ... rest of your onKeyDown code
        return super.onKeyDown(keyCode, event)
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

}