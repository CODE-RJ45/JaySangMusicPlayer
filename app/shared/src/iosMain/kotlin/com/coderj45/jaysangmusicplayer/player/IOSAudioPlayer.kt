package com.coderj45.jaysangmusicplayer.player

import com.coderj45.jaysangmusicplayer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import platform.AVFoundation.*
import platform.Foundation.NSURL
import platform.CoreMedia.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class IOSAudioPlayer : AudioPlayer {
    private var player: AVPlayer? = null
    
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

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    override fun play(song: Song, queue: List<Song>) {
        val urlStr = song.mediaUri ?: song.albumArtUrl ?: return
        val url = if (urlStr.startsWith("/")) {
            NSURL.fileURLWithPath(urlStr)
        } else {
            NSURL.URLWithString(urlStr)
        } ?: return
        
        player?.pause()
        player = AVPlayer.playerWithURL(url)
        player?.play()
        _isPlaying.value = true
        startProgressUpdate()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                player?.let { p ->
                    val currentTime = p.currentTime()
                    val seconds = CMTimeGetSeconds(currentTime)
                    if (!seconds.isNaN()) {
                         _currentPosition.value = (seconds * 1000).toLong()
                    }
                    
                    val durationTime = p.currentItem()?.duration()
                    if (durationTime != null) {
                        val durationSeconds = CMTimeGetSeconds(durationTime)
                        if (!durationSeconds.isNaN()) {
                            _duration.value = (durationSeconds * 1000).toLong()
                        }
                    }
                }
                delay(500)
            }
        }
    }

    override fun pause() {
        player?.pause()
        _isPlaying.value = false
    }

    override fun resume() {
        player?.play()
        _isPlaying.value = true
    }

    override fun seekTo(position: Long) {
        val time = CMTimeMake((position / 1000).toLong(), 1)
        player?.seekToTime(time)
    }

    override fun release() {
        player?.pause()
        player = null
        _isPlaying.value = false
        progressJob?.cancel()
    }
}
