package com.syncedapps.inthegametvexample.mediatailor

import com.google.gson.Gson
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import com.syncedapps.inthegametvexample.mediatailor.ITGM3U8Response.Companion.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MediaTailorRepo {

    private val client: OkHttpClient by lazy {
        OkHttpClient().newBuilder()
            .addInterceptor(OkHttpProfilerInterceptor())
            .build()
    }

    private val gson = Gson()

    suspend fun fetchConfig(configUrl: String, baseUrl: String): ITGM3U8 {
        return executeCall<ITGM3U8Response>(configUrl, asPost = true).toDomain(baseUrl)
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
}