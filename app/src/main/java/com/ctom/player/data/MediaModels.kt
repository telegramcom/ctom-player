package com.ctom.player.data

import android.net.Uri

enum class MediaKind { MUSIC, VIDEO }

data class MediaItemModel(
    val id: Long,
    val title: String,
    val subtitle: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val uri: Uri,
    val kind: MediaKind,
)

data class MediaLibrary(
    val songs: List<MediaItemModel> = emptyList(),
    val videos: List<MediaItemModel> = emptyList(),
) {
    val all: List<MediaItemModel> get() = songs + videos
}