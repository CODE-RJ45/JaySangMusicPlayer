package com.coderj45.jaysangmusicplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.coderj45.jaysangmusicplayer.model.Song

class LocalMusicProvider(private val context: Context) {
    fun fetchLocalSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val uris = listOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        )

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                "${MediaStore.Audio.Media.IS_ALARM} == 0 AND " +
                "${MediaStore.Audio.Media.IS_NOTIFICATION} == 0 AND " +
                "${MediaStore.Audio.Media.IS_RINGTONE} == 0 AND " +
                "${MediaStore.Audio.Media.DURATION} >= 30000" // Filter out sounds shorter than 30 seconds

        uris.forEach { contentUri ->
            val cursor = context.contentResolver.query(
                contentUri,
                projection,
                selection,
                null,
                null
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val duration = it.getLong(durationColumn)
                    val albumId = it.getLong(albumIdColumn)

                    val songUri = ContentUris.withAppendedId(contentUri, id)

                    // Album art URI
                    val albumArtUri = ContentUris.withAppendedId(
                        android.net.Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )

                    songList.add(
                        Song(
                            id = "${contentUri.hashCode()}_$id",
                            title = title,
                            artist = artist,
                            albumArtUrl = albumArtUri.toString(),
                            mediaUri = songUri.toString(),
                            duration = duration
                        )
                    )
                }
            }
        }
        return songList
    }
}
