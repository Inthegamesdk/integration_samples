package com.syncedapps.inthegametvdemo.mediatailor

data class ITGM3U8Response(
    val manifestUrl: String?,
    val trackingUrl: String?,
) {
    companion object {
        fun ITGM3U8Response.toDomain(baseUrl: String): ITGM3U8 {
            return ITGM3U8(
                baseUrl + manifestUrl,
                baseUrl + trackingUrl
            )
        }
    }
}