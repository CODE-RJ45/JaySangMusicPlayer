package com.coderj45.jaysangmusicplayer.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.coderj45.jaysangmusicplayer.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidAudioPlayer(context: Context) : AudioPlayer {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val player: Player? get() = mediaController

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

    init {
        val sessionToken = SessionToken(context, ComponentName(context, "com.coderj45.jaysangmusicplayer.PlaybackService"))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayer()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayer() {
        val p = player ?: return
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _duration.value = p.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED) {
                    scope.launch {
                        _onSongEnded.emit(Unit)
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaId.value = mediaItem?.mediaId
            }
        })
        
        // Initial state
        _isPlaying.value = p.isPlaying
        _duration.value = p.duration.coerceAtLeast(0L)
        if (p.isPlaying) startProgressUpdate()
        
        _currentMediaId.value = p.currentMediaItem?.mediaId
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                player?.let {
                    _currentPosition.value = it.currentPosition
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    override fun play(song: Song, queue: List<Song>) {
        val p = player ?: return
        
        val mediaItems = queue.map { qSong ->
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(qSong.title)
                .setArtist(qSong.artist)
            
            qSong.albumArtUrl?.let {
                metadataBuilder.setArtworkUri(Uri.parse(it))
            }

            MediaItem.Builder()
                .setUri(qSong.mediaUri ?: qSong.albumArtUrl ?: "")
                .setMediaMetadata(metadataBuilder.build())
                .setMediaId(qSong.id)
                .build()
        }

        if (mediaItems.isEmpty()) return

        val startIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        
        p.setMediaItems(mediaItems, startIndex, 0L)
        p.prepare()
        p.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun resume() {
        player?.play()
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    override fun release() {
        stopProgressUpdate()
        _isPlaying.value = false
        _currentPosition.value = 0L
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
        controllerFuture = null
    }
}
