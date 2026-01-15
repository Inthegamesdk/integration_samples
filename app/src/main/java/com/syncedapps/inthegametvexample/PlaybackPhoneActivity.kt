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
import io.inthegame.compose.ITGPlaybackComponentCompose

class PlaybackPhoneActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mediaItems = listOf(
            MediaItem.fromUri(Const.VIDEO_URL)
        )
        setContent {
            MainScreen(mediaItems = mediaItems, modifier = Modifier.safeDrawingPadding())
        }
    }

    @Composable
    fun MainScreen(
        mediaItems: List<MediaItem>,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        var player by remember { mutableStateOf<Player?>(null) }

        LifecycleStartEffect(Unit) {
            player = initializePlayer(context, mediaItems)
            onStopOrDispose {
                player?.apply { release() }
                player = null
            }
        }

        player?.let { MainScreen(player = it, modifier = modifier.fillMaxSize()) }
    }

    @Composable
    internal fun MainScreen(player: Player, modifier: Modifier = Modifier) {

        var contentScale by remember { mutableStateOf(ContentScale.Fit) }

        Box(modifier) {
            ITGPlaybackComponentCompose(
                player,
                "68650da0324217d506bcc2d4",
                "samplechannel",
                enableLogs = true,
                modifier = modifier,
                itgRequestedVideoMode = { contentScale },
                itgRequestedChangeVideoMode = { requestedContentScale ->
                    contentScale = requestedContentScale
                },
                overlayProducedAnalyticsEvent = {
                    Log.d(this@PlaybackPhoneActivity.javaClass.simpleName, "overlayProducedAnalyticsEvent $it")
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

    private fun initializePlayer(context: Context, mediaItems: List<MediaItem>): Player =
        ExoPlayer.Builder(context).build().apply {
            setMediaItems(mediaItems)
            playWhenReady = true
            prepare()
        }

}