package io.inthegame.awsdemo.mediatailor.domain.model

data class Ad(
    val startTimeInMilliseconds: Long,
    val adId : String,
    val adAsString: String
) {
    override fun toString(): String {
        return "Ad(startTimeInMilliseconds=$startTimeInMilliseconds, id=$adId)"
    }
}