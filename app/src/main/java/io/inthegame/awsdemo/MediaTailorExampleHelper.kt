package io.inthegame.awsdemo

import com.amazon.mediatailorsdk.MediaTailor
import com.amazon.mediatailorsdk.Session
import com.amazon.mediatailorsdk.SessionConfiguration
import com.amazon.mediatailorsdk.SessionError
import com.amazon.mediatailorsdk.logs.LogLevel


object MediaTailorExampleHelper {

    fun implementMediaTailor(
        contentUrl: String, onSession: (Session?, SessionError?, contentUrl: String) -> Unit
    ) {
        if (contentUrl.isEmpty() || contentUrl == "{CONTENT_URL_WITH_MT_ADS}") {
            throw IllegalStateException("Content URL is empty or invalid, please provide a valid URL")
        }

        // Implement MediaTailor SDK here
        MediaTailor.setLogLevel(LogLevel.VERBOSE)

        val config = SessionConfiguration.Builder()
            .sessionInitUrl(contentUrl)
            .build()


        MediaTailor.createSession(config) { session, error ->
            onSession(session, error, contentUrl)
        }
    }
}