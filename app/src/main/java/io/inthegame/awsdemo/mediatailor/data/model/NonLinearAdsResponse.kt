package io.inthegame.awsdemo.mediatailor.data.model

import io.inthegame.awsdemo.mediatailor.data.model.NonLinearAdResponse.Companion.toDomain
import io.inthegame.awsdemo.mediatailor.domain.model.NonLinearAds

data class NonLinearAdsResponse(
    val extensions: Any?,
    val nonLinearAdList: List<NonLinearAdResponse>,
    val trackingEvents: List<Any>
) {
    companion object {
        fun NonLinearAdsResponse.toDomain() =
            NonLinearAds(extensions, nonLinearAdList.map { it.toDomain() }, trackingEvents)
    }
}