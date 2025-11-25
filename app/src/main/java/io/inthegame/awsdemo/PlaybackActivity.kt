package io.inthegame.awsdemo

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/** Loads [PlaybackVideoFragment]. */
class PlaybackActivity : FragmentActivity() {
    private val playbackFragmentTag = "playbackFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, PlaybackVideoFragment(), playbackFragmentTag)
                .commit()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment =
                    supportFragmentManager.findFragmentByTag(playbackFragmentTag) as? PlaybackVideoFragment
                if (fragment != null && fragment.handleBackPressIfNeeded()) {
                    return
                } else {
                    // Back is pressed... Finishing the activity
                    finish()
                }
            }
        })
    }

}
