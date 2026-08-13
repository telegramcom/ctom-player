package com.ctom.player.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.PowerManager
import android.widget.RemoteViews
import com.ctom.player.R
import com.ctom.player.playback.PlaybackBus
import com.ctom.player.playback.PlaybackService

private fun progress(snapshot: com.ctom.player.playback.PlaybackSnapshot): Int =
    if (snapshot.durationMs > 0) ((snapshot.positionMs.toFloat() / snapshot.durationMs) * 1000).toInt().coerceIn(0, 1000) else 0

private fun configureServiceControls(context: Context, views: RemoteViews, mini: Boolean = false) {
    views.setOnClickPendingIntent(R.id.widget_play, PlaybackService.pendingIntent(context, PlaybackService.ACTION_TOGGLE))
    views.setOnClickPendingIntent(R.id.widget_next, PlaybackService.pendingIntent(context, PlaybackService.ACTION_NEXT))
    if (!mini) views.setOnClickPendingIntent(R.id.widget_previous, PlaybackService.pendingIntent(context, PlaybackService.ACTION_PREVIOUS))
}

abstract class PlayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
    }

    override fun onEnabled(context: Context) {
        updateAll(context)
    }

    override fun onReceive(context: Context, intent: android.content.Intent) {
        super.onReceive(context, intent)
        if (intent.action == PlaybackService.ACTION_TOGGLE ||
            intent.action == PlaybackService.ACTION_NEXT ||
            intent.action == PlaybackService.ACTION_PREVIOUS
        ) {
            updateAll(context)
        }
    }

    abstract fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int)

    protected fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = android.content.ComponentName(context, this::class.java)
        manager.getAppWidgetIds(component).forEach { update(context, manager, it) }
    }
}

class LiquidNowPlayingWidget : PlayerWidgetProvider() {
    override fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val snapshot = PlaybackBus.snapshot.value
        val views = RemoteViews(context.packageName, R.layout.widget_now_playing).apply {
            setTextViewText(R.id.widget_title, snapshot.title)
            setTextViewText(R.id.widget_artist, snapshot.subtitle)
            setProgressBar(R.id.widget_progress, 1000, progress(snapshot), false)
            setImageViewResource(R.id.widget_play, if (snapshot.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        }
        configureServiceControls(context, views)
        manager.updateAppWidget(appWidgetId, views)
    }
}

class OceanMiniPlayerWidget : PlayerWidgetProvider() {
    override fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val snapshot = PlaybackBus.snapshot.value
        val views = RemoteViews(context.packageName, R.layout.widget_mini_player).apply {
            setTextViewText(R.id.widget_title, snapshot.title)
            setTextViewText(R.id.widget_artist, snapshot.subtitle)
            setImageViewResource(R.id.widget_play, if (snapshot.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        }
        configureServiceControls(context, views, mini = true)
        manager.updateAppWidget(appWidgetId, views)
    }
}

class DeepOceanVideoWidget : PlayerWidgetProvider() {
    override fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val snapshot = PlaybackBus.snapshot.value
        val views = RemoteViews(context.packageName, R.layout.widget_video).apply {
            setTextViewText(R.id.widget_title, if (snapshot.kind == "VIDEO") snapshot.title else "Resume a video")
            setTextViewText(R.id.widget_remaining, if (snapshot.kind == "VIDEO") remaining(snapshot) else "No saved position")
            setOnClickPendingIntent(R.id.widget_play, PlaybackService.pendingIntent(context, PlaybackService.ACTION_TOGGLE))
        }
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun remaining(snapshot: com.ctom.player.playback.PlaybackSnapshot): String =
        if (snapshot.durationMs > 0) "${format(snapshot.durationMs - snapshot.positionMs)} remaining" else "Ready to play"
}

class LiquidEqualizerWidget : PlayerWidgetProvider() {
    override fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val snapshot = PlaybackBus.snapshot.value
        val powerManager = context.getSystemService(PowerManager::class.java)
        val staticMode = powerManager?.isPowerSaveMode == true
        val bars = if (staticMode) "▂▅▃▇▆▃▅▇▂▆▃▇" else "▃▆▂▇▅▃▇▆▂▅▇▃"
        val views = RemoteViews(context.packageName, R.layout.widget_equalizer).apply {
            setTextViewText(R.id.widget_title, snapshot.title)
            setTextViewText(R.id.widget_artist, if (staticMode) "Static mode · Battery Saver" else "Animated mode")
            setTextViewText(R.id.widget_bars, bars)
            setTextViewText(R.id.widget_time, "${format(snapshot.positionMs)}                         ${format(snapshot.durationMs)}")
        }
        manager.updateAppWidget(appWidgetId, views)
    }
}

class ControlCenterWidget : PlayerWidgetProvider() {
    override fun update(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_control_center).apply {
            setOnClickPendingIntent(R.id.widget_music, openApp(context, "MUSIC"))
            setOnClickPendingIntent(R.id.widget_video, openApp(context, "VIDEO"))
            setOnClickPendingIntent(R.id.widget_favorites, openApp(context, "FAVORITES"))
            setOnClickPendingIntent(R.id.widget_playlists, openApp(context, "PLAYLISTS"))
            setOnClickPendingIntent(R.id.widget_search, openApp(context, "SEARCH"))
        }
        manager.updateAppWidget(appWidgetId, views)
    }

    private fun openApp(context: Context, destination: String) =
        android.app.PendingIntent.getActivity(
            context,
            destination.hashCode(),
            android.content.Intent(context, com.ctom.player.MainActivity::class.java).putExtra("destination", destination),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
}

private fun format(milliseconds: Long): String {
    if (milliseconds <= 0) return "00:00"
    val seconds = milliseconds / 1000
    return "%02d:%02d".format(seconds / 60, seconds % 60)
}