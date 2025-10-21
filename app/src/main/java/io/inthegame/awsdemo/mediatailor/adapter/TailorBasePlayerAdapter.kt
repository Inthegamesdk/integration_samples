package io.inthegame.awsdemo.mediatailor.adapter

import androidx.annotation.CallSuper
import java.lang.ref.WeakReference


abstract class TailorBasePlayerAdapter<T>(
    var delegate: WeakReference<TailorPlayerAdapterDelegate> = WeakReference(null)
) {

    interface TailorPlayerAdapterDelegate {
        fun videoPlaying(time: Long)
        fun videoPaused(time: Long)
        fun videoSought(time: Long)
    }

    protected var player: T? = null

    abstract fun play()
    abstract fun pause()
    abstract fun seekTo(timeMs: Long)
    abstract fun isPlaying(): Boolean
    abstract val currentTime: Long

    @CallSuper
    open fun release() {
    }

    @CallSuper
    open fun onPlayerReady(player: T) {
        this.player = player
    }

    @CallSuper
    open fun onPlayerReleased() {
        this.player = null
    }

}