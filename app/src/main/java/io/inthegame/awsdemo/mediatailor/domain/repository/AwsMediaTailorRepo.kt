package io.inthegame.awsdemo.mediatailor.domain.repository

import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import io.inthegame.awsdemo.mediatailor.domain.model.NonLinearAd
import io.inthegame.awsdemo.mediatailor.domain.model.TrackingData

interface AwsMediaTailorRepo {
    suspend fun fetchConfig(configUrl: String): ITGM3U8

    suspend fun fetchTrackingData(config: ITGM3U8): TrackingData

    suspend fun fetchStaticResource(nonLinearAd: NonLinearAd): String
}