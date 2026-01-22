package io.inthegame.mediatailor

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.FragmentActivity

class InitialActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isTV(this)) {
            startActivity(Intent(this, PlaybackActivity::class.java))
            finishAffinity()
        } else {
            startActivity(Intent(this, PlaybackPhoneActivity::class.java))
            finishAffinity()
        }
    }

    private fun isTV(context: Context?): Boolean {
        val uiModeManager =
            context?.getSystemService(UI_MODE_SERVICE) as? UiModeManager ?: return false
        return (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }
}