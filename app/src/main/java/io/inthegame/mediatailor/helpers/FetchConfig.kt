package io.inthegame.mediatailor.helpers

import kotlinx.coroutines.Dispatchers
import java.net.URI

class FetchConfig : UseCase<FetchConfig.Param, ITGM3U8>(Dispatchers.IO) {

    data class Param(
        val configUrl: String,
    )

    override suspend fun execute(parameters: Param): ITGM3U8 {
        val baseUrl = extractBaseUrl(parameters.configUrl)
        return MediaTailorRepo().fetchConfig(
            parameters.configUrl.removeSuffix("/") + "/index.m3u8",
            baseUrl
        )
    }

    private fun extractBaseUrl(url: String): String {
        val uri = URI(url.trim().trim('"'))
        // scheme://host (no path/query/fragment)
        return "${uri.scheme}://${uri.host}"
    }
}