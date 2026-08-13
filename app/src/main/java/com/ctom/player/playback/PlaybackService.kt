package com.ctom.player.playback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ctom.player.data.MediaItemModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private var ticker: Job? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true,
            )
            .build()
        mediaSession = MediaSession.Builder(this, player).build()
        setMediaSession(mediaSession)

        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) = publish()
            override fun onPlaybackStateChanged(playbackState: Int) = publish()
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = publish()
        })
        ticker = CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isActive) {
                publish()
                delay(750)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_ITEM -> {
                val uri = intent.data ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE).orEmpty()
                val kind = intent.getStringExtra(EXTRA_KIND).orEmpty()
                val item = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(title)
                            .setArtist(subtitle)
                            .setMediaType(
                                if (kind == "VIDEO") MediaMetadata.MEDIA_TYPE_MOVIE
                                else MediaMetadata.MEDIA_TYPE_MUSIC,
                            )
                            .build(),
                    )
                    .build()
                player.setMediaItem(item)
                player.prepare()
                player.play()
                publish()
            }

            ACTION_TOGGLE -> if (player.isPlaying) player.pause() else player.play()
            ACTION_NEXT -> player.seekToNext()
            ACTION_PREVIOUS -> player.seekToPrevious()
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        ticker?.cancel()
        mediaSession.release()
        player.release()
        super.onDestroy()
    }

    private fun publish() {
        val metadata = player.mediaMetadata
        PlaybackBus.update(
            PlaybackSnapshot(
                title = metadata.title?.toString().orEmpty().ifBlank { "Nothing playing" },
                subtitle = metadata.artist?.toString().orEmpty().ifBlank { "Choose local media" },
                uri = player.currentMediaItem?.localConfiguration?.uri,
                kind = if (metadata.mediaType == MediaMetadata.MEDIA_TYPE_MOVIE) "VIDEO" else "MUSIC",
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.takeIf { it > 0 } ?: 0L,
            ),
        )
    }

    companion object {
        const val ACTION_PLAY_ITEM = "com.ctom.player.action.PLAY_ITEM"
        const val ACTION_TOGGLE = "com.ctom.player.action.TOGGLE"
        const val ACTION_NEXT = "com.ctom.player.action.NEXT"
        const val ACTION_PREVIOUS = "com.ctom.player.action.PREVIOUS"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_SUBTITLE = "subtitle"
        private const val EXTRA_KIND = "kind"

        fun play(context: Context, item: MediaItemModel) {
            val intent = Intent(context, PlaybackService::class.java).apply {
                action = ACTION_PLAY_ITEM
                data = item.uri
                putExtra(EXTRA_TITLE, item.title)
                putExtra(EXTRA_SUBTITLE, item.subtitle)
                putExtra(EXTRA_KIND, item.kind.name)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun command(context: Context, action: String) {
            ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java).setAction(action))
        }

        fun pendingIntent(context: Context, action: String) =
            android.app.PendingIntent.getService(
                context,
                action.hashCode(),
                Intent(context, PlaybackService::class.java).setAction(action),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
    }
}