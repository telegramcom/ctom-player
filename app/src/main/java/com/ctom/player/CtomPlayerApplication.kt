package com.ctom.player

import android.app.Application
import com.ctom.player.data.MediaRepository
import com.ctom.player.playback.PlaybackBus

class CtomPlayerApplication : Application() {
    val mediaRepository by lazy { MediaRepository(this) }

    override fun onCreate() {
        super.onCreate()
        PlaybackBus.restore(this)
    }
}