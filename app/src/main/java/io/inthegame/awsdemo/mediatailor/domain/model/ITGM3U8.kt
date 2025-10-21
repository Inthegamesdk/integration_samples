package io.inthegame.awsdemo.mediatailor.domain.model

data class ITGM3U8(
    val manifestUrl: String?,
    val trackingUrl: String?,
) {
    companion object {
        const val BASE_URL = "https://3e763f5a2cb64a869ea9bb83d5f933d7.mediatailor.us-west-2.amazonaws.com"
    }
}