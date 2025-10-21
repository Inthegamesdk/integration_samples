package io.inthegame.awsdemo.mediatailor.data.repository

import com.google.gson.Gson
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import io.inthegame.awsdemo.BuildConfig
import io.inthegame.awsdemo.mediatailor.data.model.ITGM3U8Response
import io.inthegame.awsdemo.mediatailor.data.model.ITGM3U8Response.Companion.toDomain
import io.inthegame.awsdemo.mediatailor.data.model.TrackingDataResponse
import io.inthegame.awsdemo.mediatailor.data.model.TrackingDataResponse.Companion.toDomain
import io.inthegame.awsdemo.mediatailor.domain.model.ITGM3U8
import io.inthegame.awsdemo.mediatailor.domain.model.NonLinearAd
import io.inthegame.awsdemo.mediatailor.domain.model.TrackingData
import io.inthegame.awsdemo.mediatailor.domain.repository.AwsMediaTailorRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AwsMediaTailorRepoImpl : AwsMediaTailorRepo {

    private val client: OkHttpClient by lazy {
        if (BuildConfig.DEBUG) {
            OkHttpClient().newBuilder()
                .addInterceptor(OkHttpProfilerInterceptor())
                .addInterceptor(UserAgentInterceptor())
                .build()
        } else {
            OkHttpClient().newBuilder()
                .addInterceptor(UserAgentInterceptor())
                .build()
        }
    }

    private val gson = Gson()

    override suspend fun fetchConfig(configUrl: String): ITGM3U8 {
        return executeCall<ITGM3U8Response>(BASE_URL + configUrl, asPost = true).toDomain()
    }

    override suspend fun fetchTrackingData(config: ITGM3U8): TrackingData {
        return executeCall<TrackingDataResponse>(BASE_URL + config.trackingUrl).toDomain()
    }

    override suspend fun fetchStaticResource(nonLinearAd: NonLinearAd): String {
        return executeCall(
            nonLinearAd.staticResource!!.replace(
                "https://media.inthegame.io/",
                "https://itguploadsdata.blob.core.windows.net/"
            )
        )
    }

    private suspend inline fun <reified T> executeCall(url: String, asPost: Boolean = false): T {
        var request = Request.Builder()
            .url(url)
        request = if (asPost) request.post("".toRequestBody()) else request
        return withContext(Dispatchers.IO) {
            client.newCall(request.build()).execute().let { response ->
                if (response.isSuccessful) {
                    if (T::class == String::class)
                        response.body?.string() as T
                    else
                        gson.fromJson(response.body?.string(), T::class.java)
                } else throw IllegalStateException("Response gives ${response.code}")
            }
        }
    }

    internal companion object {
        internal const val BASE_URL =
            "https://d2maq8vu6turh1.cloudfront.net";
    }
}