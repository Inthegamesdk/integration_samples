package io.inthegame.awsdemo

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import io.datazoom.sdk.Datazoom
import io.datazoom.sdk.Config.Builder
import io.datazoom.sdk.logs.LogLevel
import kotlinx.coroutines.launch

/** Loads [PlaybackVideoFragment]. */
class PlaybackActivity : FragmentActivity() {
    private val playbackFragmentTag = "playbackFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val configId = "f5562a7c-9e5f-4c60-b7c4-d174808c5d38"

        if (configId.isEmpty() || configId == "{DATAZOOM_CONFIG_ID}"){
            throw IllegalArgumentException("Please provide your Datazoom configId")
        }

        Datazoom.init(
            Builder(configId)
                .logLevel(LogLevel.VERBOSE)
                .build()
        )

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PlaybackVideoFragment(), playbackFragmentTag)
                .commit()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                lifecycleScope.launch {
                    val fragment =
                        supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
                    if (fragment != null && fragment.handleBackPressIfNeeded()) {
                        return@launch
                    } else {
                        // Back is pressed... Finishing the activity
                        finish()
                    }
                }

            }
        })
    }

}
