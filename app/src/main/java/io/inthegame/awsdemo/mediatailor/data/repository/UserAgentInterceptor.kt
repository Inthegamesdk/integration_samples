package io.inthegame.awsdemo.mediatailor.data.repository

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class UserAgentInterceptor(private val useGzip: Boolean = false) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        val builder = request.newBuilder().addHeader(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.5195.136 Mobile Safari/537.36"
        )
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Encoding", if (useGzip) "gzip" else "identify")
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "no-cache")
        request = builder.build()
        return chain.proceed(request)
    }

}