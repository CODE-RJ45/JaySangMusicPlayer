package com.coderj45.jaysangmusicplayer.player

import com.coderj45.jaysangmusicplayer.model.Song
import kotlinx.coroutines.flow.*

class JVMAudioPlayer : AudioPlayer {
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _onSongEnded = MutableSharedFlow<Unit>()
    override val onSongEnded: SharedFlow<Unit> = _onSongEnded.asSharedFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    override val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    override fun play(song: Song, queue: List<Song>) {
        _isPlaying.value = true
        _duration.value = song.duration
        _currentMediaId.value = song.id
    }

    override fun pause() {
        _isPlaying.value = false
    }

    override fun resume() {
        _isPlaying.value = true
    }

    override fun seekTo(position: Long) {
        _currentPosition.value = position
    }

    override fun release() {
        _isPlaying.value = false
    }
}
