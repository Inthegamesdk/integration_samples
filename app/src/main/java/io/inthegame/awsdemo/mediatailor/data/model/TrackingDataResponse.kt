package io.inthegame.awsdemo.mediatailor.data.model

import io.inthegame.awsdemo.mediatailor.data.model.AvailResponse.Companion.toDomain
import io.inthegame.awsdemo.mediatailor.domain.model.TrackingData

data class TrackingDataResponse(
    val avails: List<AvailResponse>,
    val dashAvailabilityStartTime: String?,
    val hlsAnchorMediaSequenceNumber: String?,
    val nextToken: String?,
    val nonLinearAvails: List<Any>,
) {
    companion object {
        fun TrackingDataResponse.toDomain() = TrackingData(
            avails.map { it.toDomain() },
            dashAvailabilityStartTime,
            hlsAnchorMediaSequenceNumber,
            nextToken,
            nonLinearAvails
        )
    }
}
