package io.inthegame.awsdemo.mediatailor

import androidx.lifecycle.LifecycleCoroutineScope
import io.inthegame.awsdemo.mediatailor.data.repository.AwsMediaTailorRepoImpl
import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import io.inthegame.awsdemo.mediatailor.domain.useCase.FetchConfig
import io.inthegame.awsdemo.mediatailor.domain.useCase.ObserveAds
import io.inthegame.awsdemo.mediatailor.domain.useCase.basic.Resource.Companion.asSuccessful
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach

class AwsMediaTailorController(
    private val lifecycleScope: LifecycleCoroutineScope,
    private val playVideo: (String?) -> Unit,
    private val displayFlexi: (flexiData: String) -> Unit
) {

    private val repo = AwsMediaTailorRepoImpl()
    private val fetchConfigUseCase = FetchConfig(repo)
    private val observeAdsUseCase = ObserveAds(repo)


    suspend fun initialize(url : String) {
        val awsConfig = fetchConfigUseCase(
            FetchConfig.Param(
                url
            )
        )
            .asSuccessful()
        playVideo(awsConfig?.manifestUrl)
        awsConfig?.let { observeAds(it) }
    }

    private fun observeAds(awsConfig: ITGM3U8) {
        observeAdsUseCase(ObserveAds.Param(awsConfig))
            .mapNotNull { it.asSuccessful() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .onEach { ad ->
                displayFlexi(ad.adAsString)
            }.launchIn(lifecycleScope)
    }

}