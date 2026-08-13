package com.ctom.player.data

import android.content.ContentResolver
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {
    suspend fun scan(): MediaLibrary = withContext(Dispatchers.IO) {
        MediaLibrary(
            songs = queryAudio(context.contentResolver),
            videos = queryVideo(context.contentResolver),
        )
    }

    private fun queryAudio(resolver: ContentResolver): List<MediaItemModel> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )
        return buildList {
            resolver.query(
                collection,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    add(
                        MediaItemModel(
                            id = id,
                            title = cursor.getString(titleColumn).orEmpty().ifBlank { "Untitled track" },
                            subtitle = cursor.getString(artistColumn).orEmpty().ifBlank { "Unknown artist" },
                            album = cursor.getString(albumColumn).orEmpty(),
                            durationMs = cursor.getLong(durationColumn),
                            uri = MediaStore.Audio.Media.getContentUri("external", id),
                            kind = MediaKind.MUSIC,
                        ),
                    )
                }
            }
        }
    }

    private fun queryVideo(resolver: ContentResolver): List<MediaItemModel> {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
        )
        return buildList {
            resolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC",
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    add(
                        MediaItemModel(
                            id = id,
                            title = cursor.getString(titleColumn).orEmpty().ifBlank { "Untitled video" },
                            subtitle = "Local video",
                            durationMs = cursor.getLong(durationColumn),
                            uri = MediaStore.Video.Media.getContentUri("external", id),
                            kind = MediaKind.VIDEO,
                        ),
                    )
                }
            }
        }
    }
}