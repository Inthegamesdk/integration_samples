package io.inthegame.awsdemo.mediatailor.data.model

import io.inthegame.awsdemo.mediatailor.data.model.NonLinearAdsResponse.Companion.toDomain
import io.inthegame.awsdemo.mediatailor.domain.model.Avail
import kotlin.math.roundToLong

data class AvailResponse(
    val adBreakTrackingEvents: List<Any>,
    val adMarkerDuration: String?,
    val ads: List<Any>,
    val availId: String,
    val availProgramDateTime: String?,
    val duration: String,
    val durationInSeconds: Int,
    val meta: Any?,
    val nonLinearAdsList: List<NonLinearAdsResponse>,
    val startTime: String,
    val startTimeInSeconds: Double
) {
    companion object {
        fun AvailResponse.toDomain(): Avail {
            return Avail(
                adBreakTrackingEvents,
                adMarkerDuration,
                ads,
                availId,
                availProgramDateTime,
                duration,
                durationInSeconds,
                meta,
                nonLinearAdsList.map { it.toDomain() },
                startTime,
                (startTimeInSeconds * 1000).roundToLong()
            )
        }
    }
}