package io.inthegame.awsdemo.mediatailor.domain.model

import io.inthegame.awsdemo.mediatailor.domain.model.NonLinearAd

data class NonLinearAds(
    val extensions: Any?,
    val nonLinearAdList: List<NonLinearAd>,
    val trackingEvents: List<Any>
)