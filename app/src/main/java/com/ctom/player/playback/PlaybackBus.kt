package com.ctom.player.playback

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaybackSnapshot(
    val title: String = "Nothing playing",
    val subtitle: String = "Choose local media",
    val uri: Uri? = null,
    val kind: String = "MUSIC",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

object PlaybackBus {
    private val _snapshot = MutableStateFlow(PlaybackSnapshot())
    val snapshot: StateFlow<PlaybackSnapshot> = _snapshot.asStateFlow()

    fun update(snapshot: PlaybackSnapshot) {
        _snapshot.value = snapshot
    }

    fun restore(context: Context) {
        // Playback state is intentionally process-local. MediaSession remains the source
        // of truth after a process restart and restores the system notification session.
    }
}