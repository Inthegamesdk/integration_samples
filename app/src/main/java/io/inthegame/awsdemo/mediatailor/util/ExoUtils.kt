package io.inthegame.awsdemo.mediatailor.util

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import java.lang.reflect.Field

object ExoUtils {

    val Player?.periodPositionMs: Long?
        get() {
            return if (this is ExoPlayer)
                this.periodPositionMs
            else
                null
        }

    val ExoPlayer?.periodPositionMs: Long?
        get() {
            val player = this ?: return null

            val internalPlayerImpl = readNonAccessibleField<Any>(
                "com.google.android.exoplayer2.ExoPlayerImpl",
                "internalPlayer",
                player
            ) ?: return null

            val playbackInfo = readNonAccessibleField<Any>(
                "com.google.android.exoplayer2.ExoPlayerImplInternal",
                "playbackInfo",
                internalPlayerImpl
            ) ?: return null

            val positionUs = readNonAccessibleField<Long>(
                "com.google.android.exoplayer2.PlaybackInfo",
                "positionUs",
                playbackInfo
            )

            return positionUs?.let { it / 1000 }
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T> readNonAccessibleField(
        className: String,
        fieldName: String,
        dataProperty: Any
    ): T? {
        val classRef = Class.forName(className)

        // Create Field object
        val fieldRef: Field = classRef.getDeclaredField(fieldName)

        val isAccessible = fieldRef.isAccessible
        // Set the accessibility as true
        fieldRef.isAccessible = true

        // Store the value of private field in variable
        val value = fieldRef.get(dataProperty)

        // Return the accessibility back
        fieldRef.isAccessible = isAccessible

        return value as? T
    }
}