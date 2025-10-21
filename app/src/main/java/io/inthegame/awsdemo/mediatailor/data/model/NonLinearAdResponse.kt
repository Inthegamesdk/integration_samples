package io.inthegame.awsdemo.mediatailor.data.model

import io.inthegame.awsdemo.mediatailor.domain.model.NonLinearAd

data class NonLinearAdResponse(
    val adId: String?,
    val adParameters: String?,
    val adSystem: String?,
    val adTitle: String?,
    val apiFramework: String?,
    val clickThrough: String?,
    val clickTracking: String?,
    val clickTrackingId: String?,
    val creativeAdId: String?,
    val creativeId: String?,
    val creativeSequence: String?,
    val duration: String?,
    val durationInSeconds: String?,
    val expandedHeight: String?,
    val expandedWidth: String?,
    val height: String?,
    val htmlResource: String?,
    val iFrameResource: String?,
    val maintainAspectRatio: Boolean?,
    val minSuggestedDuration: String?,
    val scalable: Boolean?,
    val staticResource: String?,
    val staticResourceCreativeType: String?,
    val width: String?,
) {
    companion object {
        fun NonLinearAdResponse.toDomain() = NonLinearAd(
            adId,
            adParameters,
            adSystem,
            adTitle,
            apiFramework,
            clickThrough,
            clickTracking,
            clickTrackingId,
            creativeAdId,
            creativeId,
            creativeSequence,
            duration,
            durationInSeconds,
            expandedHeight,
            expandedWidth,
            height,
            htmlResource,
            iFrameResource,
            maintainAspectRatio,
            minSuggestedDuration,
            scalable,
            staticResource,
            staticResourceCreativeType,
            width
        )
    }
}