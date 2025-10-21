package io.inthegame.awsdemo.mediatailor.domain.model

import io.inthegame.awsdemo.mediatailor.domain.model.Avail

data class TrackingData(
    val avails: List<Avail>,
    val dashAvailabilityStartTime: String?,
    val hlsAnchorMediaSequenceNumber: String?,
    val nextToken: String?,
    val nonLinearAvails: List<Any>
)
