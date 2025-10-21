package io.inthegame.awsdemo.mediatailor.domain.useCase

import io.inthegame.awsdemo.mediatailor.data.repository.AwsMediaTailorRepoImpl
import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import io.inthegame.awsdemo.mediatailor.domain.repository.AwsMediaTailorRepo
import io.inthegame.awsdemo.mediatailor.domain.useCase.basic.UseCase
import kotlinx.coroutines.Dispatchers

class FetchConfig(
    private val awsMediaTailorRepo: AwsMediaTailorRepo
) : UseCase<FetchConfig.Param, ITGM3U8>(Dispatchers.IO) {

    data class Param(
        val configUrl: String
    )

    override suspend fun execute(parameters: Param): ITGM3U8 {
        return awsMediaTailorRepo.fetchConfig(parameters.configUrl).let { config ->
            config.copy(
                manifestUrl = AwsMediaTailorRepoImpl.BASE_URL + config.manifestUrl, //prepare m3u8 path
            )
        }
    }
}