package io.inthegame.awsdemo.mediatailor.domain.model

data class Avail(
    val adBreakTrackingEvents: List<Any>,
    val adMarkerDuration: String?,
    val ads: List<Any>,
    val availId: String,
    val availProgramDateTime: String?,
    val duration: String,
    val durationInSeconds: Int,
    val meta: Any?,
    val nonLinearAdsList: List<NonLinearAds>,
    val startTime: String,
    val startTimeInMilliseconds: Long
)