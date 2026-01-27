package com.syncedapps.inthegametvexample

import android.annotation.SuppressLint
import android.content.Context
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
import com.syncedapps.inthegametvexample.databinding.ActivityPhonePlaybackBinding
import android.view.ViewGroup
import java.util.*

import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.activity.OnBackPressedCallback
import android.view.KeyEvent
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.buttons.MuteButton
import androidx.media3.ui.compose.material3.buttons.NextButton
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.PreviousButton
import androidx.media3.ui.compose.material3.buttons.RepeatButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import androidx.media3.ui.compose.material3.buttons.ShuffleButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import com.amazon.mediatailorsdk.AdTrackingUpdateMode
import com.amazon.mediatailorsdk.MediaTailor
import com.amazon.mediatailorsdk.Session
import com.amazon.mediatailorsdk.SessionConfiguration
import io.datazoom.sdk.Config.Builder
import io.datazoom.sdk.Datazoom
import io.datazoom.sdk.DzAdapter
import io.datazoom.sdk.logs.LogLevel
import io.datazoom.sdk.media3.createContext
import io.datazoom.sdk.mediatailor.setupAdSession
import io.inthegame.compose.ITGPlaybackComponentCompose
import io.inthegame.datazoom.ITGDatazoomPlugin.attachITG

class PlaybackPhoneActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configId = "f5562a7c-9e5f-4c60-b7c4-d174808c5d38"

        if (configId.isEmpty() || configId == "{DATAZOOM_CONFIG_ID}") {
            throw IllegalArgumentException("Please provide your Datazoom configId")
        }

        Datazoom.init(
            Builder(configId)
                .logLevel(LogLevel.VERBOSE)
                .build()
        )

        setContent {
            MainScreen(modifier = Modifier.safeDrawingPadding())
        }
    }

    @Composable
    fun MainScreen(
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current

        var player by remember { mutableStateOf<Player?>(null) }
        var session by remember { mutableStateOf<Session?>(null) }
        var datazoomAdapter by remember { mutableStateOf<DzAdapter?>(null) }

        LifecycleStartEffect(Unit) {

            // Implement MediaTailor SDK here
            MediaTailor.setLogLevel(com.amazon.mediatailorsdk.logs.LogLevel.VERBOSE)

            player = initializePlayer(context)
            // Datazoom start
            datazoomAdapter = Datazoom.createContext(exoPlayer = player as ExoPlayer)
            // Datazoom end

            val config = SessionConfiguration.Builder()
                .sessionInitUrl(CONTENT_URL)
                .adTrackingUpdateMode(AdTrackingUpdateMode.FULL)

                .build()

            MediaTailor.createSession(config) { sessionValue, _ ->
                sessionValue?.let {
                    session = sessionValue
                    datazoomAdapter?.setupAdSession(sessionValue, View(context), CONTENT_URL)

                    player?.setMediaItem(MediaItem.fromUri(sessionValue.playbackUrl.orEmpty().toUri()))
                }
            }

            onStopOrDispose {
                player?.apply { release() }
                player = null
            }
        }

        player?.let { playerInstance ->
            session?.let { sessionInstance ->
                MainScreen(player = playerInstance, session = sessionInstance, modifier = modifier.fillMaxSize())
            }
        }
    }

    @Composable
    internal fun MainScreen(player: Player, session : Session, modifier: Modifier = Modifier) {

        var contentScale by remember { mutableStateOf(ContentScale.Fit) }

        Box(modifier.background(Color.Black)) {
            ITGPlaybackComponentCompose(
                player,
                "69230d1b5f7b3515524dd184",
                "demo_mediatailor",
                enableLogs = true,
                modifier = modifier,
                itgRequestedVideoMode = { contentScale },
                itgPlaybackComponentCreated = { itgPlaybackComponent ->
                    //Datazoom plugin init
                   session.attachITG(itgPlaybackComponent)
                },
                itgRequestedChangeVideoMode = { requestedContentScale ->
                    contentScale = requestedContentScale
                },
                overlayProducedAnalyticsEvent = {
                    Log.d(
                        this@PlaybackPhoneActivity.javaClass.simpleName,
                        "overlayProducedAnalyticsEvent $it"
                    )
                },
                onBackPressed = {
                    finish()
                }
            ) {
                MediaPlayer(
                    player,
                    contentScale
                )
            }
        }
    }


    @Composable
    internal fun BoxScope.MediaPlayer(
        player: Player,
        contentScale: ContentScale
    ) {
        var showControls by remember { mutableStateOf(true) }
        ContentFrame(
            player = player,
            modifier = Modifier.noRippleClickable { showControls = !showControls },
            contentScale = contentScale
        )

        if (showControls) {
            // drawn on top of a potential shutter
            Controls(player)
        }
    }

    @Composable
    private fun RowControls(
        modifier: Modifier = Modifier,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
        verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
        additionalSpacer: Float? = null,
        buttons: List<@Composable () -> Unit>,
    ) {
        Row(modifier, horizontalArrangement, verticalAlignment) {
            buttons.forEachIndexed { index, button ->
                button()
                if (index < buttons.lastIndex && additionalSpacer != null) {
                    Spacer(Modifier.weight(additionalSpacer))
                }
            }
        }
    }

    @Composable
    internal fun BoxScope.Controls(player: Player) {
        val buttonModifier = Modifier
            .size(50.dp)
            .background(Color.Gray.copy(alpha = 0.1f), CircleShape)
        // Central controls
        RowControls(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            buttons =
                listOf(
                    { PreviousButton(player, buttonModifier) },
                    { SeekBackButton(player, buttonModifier) },
                    { PlayPauseButton(player, buttonModifier) },
                    { SeekForwardButton(player, buttonModifier) },
                    { NextButton(player, buttonModifier) },
                ),
        )
        // Button panel controls
        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.4f))
                        .padding(start = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PositionAndDurationText(player)
                Spacer(Modifier.weight(1f))
                ShuffleButton(player)
                RepeatButton(player)
                MuteButton(player)
            }
        }
    }

    @Composable
    internal fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // to prevent the ripple from the tap
        ) {
            onClick()
        }

    private fun initializePlayer(context: Context): Player =
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            prepare()
        }

    companion object {
        const val CONTENT_URL =
            "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/index.m3u8"
    }

}