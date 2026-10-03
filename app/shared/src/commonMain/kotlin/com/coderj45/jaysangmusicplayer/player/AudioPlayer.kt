package com.coderj45.jaysangmusicplayer.player

import com.coderj45.jaysangmusicplayer.model.Song
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>
    val onSongEnded: SharedFlow<Unit>
    val currentMediaId: StateFlow<String?>

    fun play(song: Song, queue: List<Song> = emptyList())
    fun pause()
    fun resume()
    fun seekTo(position: Long)
    fun release()
}
