package com.coderj45.jaysangmusicplayer.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String> = emptyList()
)
