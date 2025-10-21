package io.inthegame.awsdemo.mediatailor.adapter

import com.google.android.exoplayer2.Player
import java.lang.ref.WeakReference

class TailorExoPlayerAdapter(
    delegate: WeakReference<TailorPlayerAdapterDelegate> = WeakReference(null)
) : TailorBaseExoPlayerAdapter(delegate), Player.Listener