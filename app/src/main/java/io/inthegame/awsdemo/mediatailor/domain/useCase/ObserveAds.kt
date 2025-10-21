package io.inthegame.awsdemo.mediatailor.domain.useCase

import android.util.Log
import io.inthegame.awsdemo.mediatailor.domain.model.Ad
import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import io.inthegame.awsdemo.mediatailor.domain.repository.AwsMediaTailorRepo
import io.inthegame.awsdemo.mediatailor.domain.useCase.basic.FlowUseCase
import io.inthegame.awsdemo.mediatailor.domain.useCase.basic.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.util.Date
import kotlin.coroutines.coroutineContext

class ObserveAds(
    private val awsMediaTailorRepo: AwsMediaTailorRepo
) : FlowUseCase<ObserveAds.Param, Ad>(Dispatchers.IO) {

    data class Param(
        val config: ITGM3U8,
    )

    private val consumedAvailsId: MutableList<String> = mutableListOf()

    private var lastFireTime : Long = 0

    override fun execute(parameters: Param): Flow<Resource<Ad>> {
        val (config) = parameters
        return flow {
            while (coroutineContext.isActive) {
                try {
                    val trackingData = awsMediaTailorRepo.fetchTrackingData(config)
                    val avails =
                        trackingData.avails.filter { !consumedAvailsId.contains(it.availId) }
                    val avail = avails.firstOrNull { avail ->
                        avail.nonLinearAdsList.isNotEmpty() && avail.nonLinearAdsList[0].nonLinearAdList.isNotEmpty()
                    }
                    Log.d("ObserveAds", "avail $avail $avails")
                    avail?.let {
                        if (Date().time - lastFireTime >= MIN_AD_INTERVAL) {
                            consumedAvailsId.add(avail.availId)
                            emit(
                                Resource.Success(
                                    Ad(
                                        avail.startTimeInMilliseconds,
                                        avail.nonLinearAdsList[0].nonLinearAdList[0].adId.orEmpty(),
                                        awsMediaTailorRepo.fetchStaticResource(avail.nonLinearAdsList[0].nonLinearAdList[0]),
                                    )
                                )
                            )
                            lastFireTime = Date().time
                        }
                    }

                } catch (e: Exception) {
                    emit(Resource.Failure(e))
                }
                delay(INTERVAL)
            }
        }
    }

    private companion object {
        private const val INTERVAL = 5_000L
        private const val MIN_AD_INTERVAL = 15_000L
    }
}