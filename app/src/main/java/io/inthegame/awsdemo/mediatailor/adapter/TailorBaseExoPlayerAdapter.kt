package io.inthegame.awsdemo.mediatailor.adapter

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import io.inthegame.awsdemo.mediatailor.util.ExoUtils.periodPositionMs
import java.lang.ref.WeakReference

abstract class TailorBaseExoPlayerAdapter(
    delegate: WeakReference<TailorPlayerAdapterDelegate> = WeakReference(null)
) : TailorBasePlayerAdapter<ExoPlayer>(delegate), Player.Listener {

    override fun isPlaying() =
        player?.isPlaying == true && player?.playbackState == Player.STATE_READY

    override fun pause() {
        player?.pause()
    }

    override fun play() {
        player?.play()
    }

    override fun seekTo(timeMs: Long) {
        player?.seekTo(timeMs)
    }

    override val currentTime: Long
        get() {
            return player.periodPositionMs ?: player?.currentPosition ?: 0L
        }

    // ExoPlayer events
    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        delegate.get()?.videoSought(currentTime)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        val player = player ?: return
        /** inform the overlay when the player is ready to achieve the best clocks' precision **/
        if (playbackState == Player.STATE_READY && player.playWhenReady) {
            delegate.get()?.videoPlaying(currentTime)
        } else if (playbackState != Player.STATE_READY) {
            delegate.get()?.videoPaused(currentTime) //let's pause any scheduling
        } else {
            //do nothing
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
        player?.playbackState?.let { onPlaybackStateChanged(it) }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        val player = player ?: return
        if (isPlaying)
            delegate.get()?.videoPlaying(player.currentPosition)
        else
            delegate.get()?.videoPaused(player.currentPosition)
    }

    override fun onPlayerReady(player: ExoPlayer) {
        super.onPlayerReady(player)
        player.addListener(this)
    }

    override fun onPlayerReleased() {
        player?.removeListener(this)
        super.onPlayerReleased()
        player = null
    }

}