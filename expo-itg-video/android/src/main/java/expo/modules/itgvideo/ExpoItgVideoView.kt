package expo.modules.itgvideo

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.syncedapps.inthegametv.integration.ITGMedia3PlayerAdapter
import com.syncedapps.inthegametv.integration.ITGPlaybackComponent
import androidx.lifecycle.findViewTreeLifecycleOwner

class ExpoItgVideoView(context: Context, appContext: AppContext) : ExpoView(context, appContext), LifecycleOwner {
  private var playerView: PlayerView
  private var player: ExoPlayer

  private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

  init {
    lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    onAppear()
    initITG()
  }

  override fun onDetachedFromWindow() {
    onDisappear()
    super.onDetachedFromWindow()
  }

  override fun requestLayout() {
    super.requestLayout()
    post {
        val w = measuredWidth
        val h = measuredHeight
        if (w > 0 && h > 0) {
            measure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            )
            layout(left, top, left + w, top + h)
        }
    }
  }

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  private fun onAppear() {
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
  }

  private fun onDisappear() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
  }

  private fun initITG() {
    // Replace 'your_account_id' and 'your_channel_slug' with actual values
    val accountId = "68650da0324217d506bcc2d4"
    val channelSlug = "samplechannel"


    // Initialize ITGPlaybackComponent
    val mITGComponent = ITGPlaybackComponent(context)


    // Set up the ITGMedia3PlayerAdapter with your player view
    val adapter = ITGMedia3PlayerAdapter(playerView = playerView)
    adapter.onPlayerReady(player)

    val owner = this.findViewTreeLifecycleOwner() ?: return

    // Initialize the ITG component with necessary parameters
    mITGComponent.init(
      root = this,
      lifecycleOwner = owner,
      playerAdapter = adapter, //mandatory: adapter between the player and SDK
      savedState = null, //mandatory: saved state of the component

      accountId = accountId, //mandatory: your ITG accountId
      channelSlug = channelSlug, //mandatory: your channelId on our admin panel
    )


    // Add the ITG component to your view hierarchy
    addView(mITGComponent, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
  }

  init {
    layoutParams = FrameLayout.LayoutParams(
      LayoutParams.MATCH_PARENT,
      LayoutParams.MATCH_PARENT
    )

    player = ExoPlayer.Builder(context).build()
    playerView = PlayerView(context)
    playerView.player = player
    playerView.useController = true // show default controls
    addView(playerView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

    val value = "https://assets.inthegame.io/Liran/IBC_F1_V2.mp4"
    val mediaItem = MediaItem.fromUri(Uri.parse(value))
    player.setMediaItem(mediaItem)
    player.playWhenReady = true
    player.prepare()
  }
}
