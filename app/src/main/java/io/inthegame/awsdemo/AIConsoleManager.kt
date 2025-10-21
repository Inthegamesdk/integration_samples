package io.inthegame.awsdemo

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.*

class AIConsoleManager(
    private val context: Context,
    private val rootView: ViewGroup,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    private val consoleLayout = FrameLayout(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            480,
            400,
            Gravity.BOTTOM or Gravity.END
        )
        setBackgroundColor(Color.parseColor("#CC000000")) // semi-transparent black
        alpha = 0.8f
        visibility = View.INVISIBLE
        setPadding(24, 24, 24, 24)
    }

    private val itgLogo = ImageView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            100,
            100,
            Gravity.BOTTOM or Gravity.END
        )
        setImageResource(R.drawable.itg_logo_transparent)
        setPadding(6, 6, 6, 6)
    }

    private val innerLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    private val consoleText = TextView(context).apply {
        setTextColor(Color.GREEN)
        typeface = ResourcesCompat.getFont(context, R.font.exo2_light)
        textSize = 14f
        setLineSpacing(5F, 1F)
    }

    private val progressBar =
        ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT)
            isIndeterminate = true
            visibility = View.GONE
        }

    private val resultTypes = listOf("Tickets", "Merch", "Betting")

    init {
        innerLayout.addView(consoleText)
        innerLayout.addView(progressBar)
        consoleLayout.addView(innerLayout)
        consoleLayout.addView(itgLogo)
        rootView.addView(consoleLayout)
    }

    private var job: Job? = null
    fun startConsoleFlow() {
        job?.cancel()
        job = lifecycleScope.launch {
            consoleText.text = ""
            fadeIn(consoleLayout)
            delay(1_000L)
            updateConsole("Analyzing video stream\nSearching advertising moment")
            delay(1_000L)
            progressBar.visibility = View.VISIBLE
        }
    }

    fun startDisplayAdFlow() {
        job?.cancel()
        job = lifecycleScope.launch {
            val adType = resultTypes.random()
            progressBar.visibility = View.GONE
            updateConsole("Ad moment found = \"$adType\"")
            delay(1_000)
            fadeOut(consoleLayout)
        }
    }

    private suspend fun updateConsole(text: String) {
        consoleText.text = ""
        for (i in text.indices) {
            consoleText.text = text.substring(0, i + 1) + " |"
            delay(if (text[i] == '\n') 1_000L else 50)
        }
        consoleText.text = text
    }


    private fun fadeIn(view: View) {
        view.visibility = View.VISIBLE
        val fade = AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        }
        view.startAnimation(fade)
    }

    private fun fadeOut(view: View) {
        view.visibility = View.VISIBLE
        val fade = AlphaAnimation(1f, 0f).apply {
            duration = 500
            fillAfter = true
        }
        view.startAnimation(fade)
    }
}
