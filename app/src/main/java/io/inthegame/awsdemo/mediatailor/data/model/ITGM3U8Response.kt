package io.inthegame.awsdemo.mediatailor.data.model

import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8

data class ITGM3U8Response(
    val manifestUrl: String?,
    val trackingUrl: String?,
) {
    companion object {
        fun ITGM3U8Response.toDomain() = ITGM3U8(ITGM3U8.BASE_URL + manifestUrl, ITGM3U8.BASE_URL + trackingUrl)
    }
}