package com.coderj45.jaysangmusicplayer.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val songs: List<Song>,
    val playlists: List<Playlist>,
    val exportTimestamp: Long,
    val deviceName: String? = null
)
