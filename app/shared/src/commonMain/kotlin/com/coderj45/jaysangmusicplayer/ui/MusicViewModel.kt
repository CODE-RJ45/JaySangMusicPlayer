package com.coderj45.jaysangmusicplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderj45.jaysangmusicplayer.data.Storage
import com.coderj45.jaysangmusicplayer.data.PlaylistRepository
import com.coderj45.jaysangmusicplayer.data.SongRepository
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.BackupData
import com.coderj45.jaysangmusicplayer.player.AudioPlayer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

enum class RepeatMode {
    NONE, ALL, ONE
}

enum class SortOrder {
    TITLE, ARTIST, DURATION
}

class MusicViewModel(
    private val audioPlayer: AudioPlayer? = null,
    private val storage: Storage? = null,
    private val shareManager: ShareManager? = null,
    private val repository: SongRepository = SongRepository(storage),
    private val playlistRepository: PlaylistRepository = PlaylistRepository(storage),
    initialSongs: List<Song> = emptyList()
) : ViewModel() {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentSource = MutableStateFlow("Local Library")
    val currentSource: StateFlow<String> = _currentSource.asStateFlow()

    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed

    val isPlaying: StateFlow<Boolean> = audioPlayer?.isPlaying ?: MutableStateFlow(false)
    val playbackPosition: StateFlow<Long> = audioPlayer?.currentPosition ?: MutableStateFlow(0L)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(storage?.getString("shuffle_enabled")?.toBoolean() ?: false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(
        RepeatMode.valueOf(storage?.getString("repeat_mode") ?: RepeatMode.NONE.name)
    )
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    private val shuffledQueue = mutableListOf<Song>()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Settings
    private val _specialName = MutableStateFlow(storage?.getString("special_name") ?: "Love")
    val specialName: StateFlow<String> = _specialName.asStateFlow()

    private val _specialMonth = MutableStateFlow(storage?.getString("special_month")?.toInt() ?: 8)
    val specialMonth: StateFlow<Int> = _specialMonth.asStateFlow()

    private val _specialDay = MutableStateFlow(storage?.getString("special_day")?.toInt() ?: 29)
    val specialDay: StateFlow<Int> = _specialDay.asStateFlow()

    private val _gaplessPlayback = MutableStateFlow(storage?.getString("gapless_playback")?.toBoolean() ?: true)
    val gaplessPlayback: StateFlow<Boolean> = _gaplessPlayback.asStateFlow()

    private val _themeMode = MutableStateFlow(storage?.getString("theme_mode") ?: "Dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    val songs: StateFlow<List<Song>> = combine(repository.localSongs, _searchQuery, _sortOrder) { songs, query, order ->
        val filtered = if (query.isBlank()) {
            songs
        } else {
            songs.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.artist.contains(query, ignoreCase = true)
            }
        }
        
        when (order) {
            SortOrder.TITLE -> filtered.sortedBy { it.title }
            SortOrder.ARTIST -> filtered.sortedBy { it.artist }
            SortOrder.DURATION -> filtered.sortedBy { it.duration }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialSongs)

    init {
        if (initialSongs.isNotEmpty()) {
            repository.updateSongs(initialSongs)
        }
        
        // Auto play next song when current one ends
        viewModelScope.launch {
            audioPlayer?.onSongEnded?.collect {
                playNext()
            }
        }

        // Handle transitions from system notification (MediaSession)
        viewModelScope.launch {
            audioPlayer?.currentMediaId?.collect { mediaId ->
                val currentId = _currentSong.value?.id
                if (mediaId != null && mediaId != currentId) {
                    // Try to find in current queue first
                    var song = _currentQueue.value.find { it.id == mediaId }
                    
                    // If not in queue (e.g. app restart), find in all songs and set as queue
                    if (song == null) {
                        val allSongs = repository.getSongs()
                        song = allSongs.find { it.id == mediaId }
                        if (song != null) {
                            _currentQueue.value = allSongs
                        }
                    }
                    
                    if (song != null) {
                        _currentSong.value = song
                        repository.addToRecentlyPlayed(song)
                    }
                }
            }
        }
    }

    fun loadSongs(songs: List<Song>) {
        repository.updateSongs(songs)
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun selectSong(song: Song, queue: List<Song> = emptyList(), sourceName: String = "Local Library") {
        _currentSong.value = song
        _currentSource.value = sourceName
        if (queue.isNotEmpty()) {
            _currentQueue.value = queue
            if (_isShuffleEnabled.value) {
                shuffledQueue.clear()
                shuffledQueue.addAll(queue.shuffled())
            }
        } else if (_currentQueue.value.isEmpty() || !_currentQueue.value.contains(song)) {
            val allSongs = repository.getSongs()
            _currentQueue.value = allSongs
            if (_isShuffleEnabled.value) {
                shuffledQueue.clear()
                shuffledQueue.addAll(allSongs.shuffled())
            }
        }
        
        // Update recently played via repository for persistence
        repository.addToRecentlyPlayed(song)

        val playUrl = song.mediaUri ?: song.albumArtUrl ?: ""
        if (playUrl.isNotEmpty()) {
            audioPlayer?.play(song, _currentQueue.value)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            audioPlayer?.pause()
        } else {
            audioPlayer?.resume()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleShuffle() {
        val newValue = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newValue
        storage?.setString("shuffle_enabled", newValue.toString())
        
        if (newValue) {
            shuffledQueue.clear()
            shuffledQueue.addAll(_currentQueue.value.shuffled())
        }
    }

    fun toggleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
        _repeatMode.value = newMode
        storage?.setString("repeat_mode", newMode.name)
    }

    fun playNext() {
        val currentList = _currentQueue.value
        if (currentList.isEmpty()) return

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        val nextSong = when {
            _repeatMode.value == RepeatMode.ONE && _currentSong.value != null -> _currentSong.value
            _isShuffleEnabled.value -> {
                if (shuffledQueue.isEmpty()) {
                    shuffledQueue.addAll(currentList.shuffled())
                }
                val song = shuffledQueue.firstOrNull { it.id != _currentSong.value?.id } ?: shuffledQueue.firstOrNull()
                shuffledQueue.remove(song)
                song
            }
            currentIndex != -1 && currentIndex < currentList.size - 1 -> currentList[currentIndex + 1]
            _repeatMode.value == RepeatMode.ALL -> currentList.firstOrNull()
            else -> null
        }
        
        nextSong?.let { selectSong(it, currentList, _currentSource.value) }
    }

    fun playPrevious() {
        val currentList = _currentQueue.value
        if (currentList.isEmpty()) return

        val currentIndex = currentList.indexOfFirst { it.id == _currentSong.value?.id }
        
        val prevSong = when {
            _isShuffleEnabled.value -> currentList.random()
            currentIndex > 0 -> currentList[currentIndex - 1]
            _repeatMode.value == RepeatMode.ALL -> currentList.lastOrNull()
            else -> currentList.firstOrNull()
        }
        
        prevSong?.let { selectSong(it, currentList, _currentSource.value) }
    }

    fun seekTo(position: Long) {
        audioPlayer?.seekTo(position)
    }

    fun updateAlbumArt(songId: String, imageUrl: String) {
        _currentSong.value?.let { current ->
            if (current.id == songId) {
                val updated = current.copy(albumArtUrl = imageUrl)
                _currentSong.value = updated
                repository.updateSong(updated)
            }
        } ?: run {
            repository.getSongs().find { it.id == songId }?.let { song ->
                repository.updateSong(song.copy(albumArtUrl = imageUrl))
            }
        }
    }

    fun updateSongInfo(songId: String, newTitle: String, newArtist: String) {
        _currentSong.value?.let { current ->
            if (current.id == songId) {
                val updated = current.copy(title = newTitle, artist = newArtist)
                _currentSong.value = updated
                repository.updateSong(updated)
            }
        } ?: run {
            repository.getSongs().find { it.id == songId }?.let { song ->
                repository.updateSong(song.copy(title = newTitle, artist = newArtist))
            }
        }
    }

    fun updateSongNote(songId: String, note: String?) {
        _currentSong.value?.let { current ->
            if (current.id == songId) {
                val updated = current.copy(note = note)
                _currentSong.value = updated
                repository.updateSong(updated)
            }
        } ?: run {
            repository.getSongs().find { it.id == songId }?.let { song ->
                repository.updateSong(song.copy(note = note))
            }
        }
    }

    fun updateSongLyrics(songId: String, lyrics: String?) {
        _currentSong.value?.let { current ->
            if (current.id == songId) {
                val updated = current.copy(lyrics = lyrics)
                _currentSong.value = updated
                repository.updateSong(updated)
            }
        } ?: run {
            repository.getSongs().find { it.id == songId }?.let { song ->
                repository.updateSong(song.copy(lyrics = lyrics))
            }
        }
    }

    fun removeSongFromLibrary(songId: String) {
        if (_currentSong.value?.id == songId) {
            playNext()
            if (_currentSong.value?.id == songId) {
                _currentSong.value = null
            }
        }
        repository.deleteSong(songId)
    }

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun saveStoryToGallery() {
        _currentSong.value?.let { song ->
            viewModelScope.launch {
                val path = shareManager?.saveStoryImageToGallery(song)
                if (path != null) {
                    _snackbarMessage.emit("Saved to Gallery: Pictures/JaySangMusic")
                } else {
                    _snackbarMessage.emit("Failed to save image")
                }
            }
        }
    }

    suspend fun fetchLyricsOnline(title: String, artist: String): String? {
        return try {
            val response: LrcLibResponse = httpClient.get("https://lrclib.net/api/get") {
                parameter("track_name", title)
                parameter("artist_name", artist)
            }.body()
            
            response.syncedLyrics ?: response.plainLyrics
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Serializable
    private data class LrcLibResponse(
        val plainLyrics: String? = null,
        val syncedLyrics: String? = null
    )

    fun exportBackup() {
        viewModelScope.launch {
            val backupData = BackupData(
                songs = repository.getSongs(),
                playlists = playlists.value,
                exportTimestamp = 0L // Fallback
            )
            val result = shareManager?.exportFullPackage(backupData)
            if (result != null) {
                _snackbarMessage.emit("Preparing full package for sharing...")
            } else {
                _snackbarMessage.emit("Failed to create backup")
            }
        }
    }

    fun importBackup(uri: String) {
        viewModelScope.launch {
            _snackbarMessage.emit("Importing package... please wait")
            val backupData = shareManager?.importFullPackage(uri)
            if (backupData != null) {
                // 1. Restore Songs
                repository.updateSongs(backupData.songs)
                
                // 2. Restore Playlists (Smart Merge)
                backupData.playlists.forEach { importedPlaylist ->
                    val existing = playlists.value.find { it.name == importedPlaylist.name }
                    if (existing == null) {
                        // Create new if it doesn't exist
                        createPlaylist(importedPlaylist.name)
                        // Get the newly created playlist to add songs
                        playlists.value.lastOrNull()?.let { newPlaylist ->
                            importedPlaylist.songIds.forEach { songId ->
                                addSongToPlaylist(newPlaylist.id, songId)
                            }
                        }
                    } else {
                        // Merge songs into existing playlist
                        importedPlaylist.songIds.forEach { songId ->
                            if (!existing.songIds.contains(songId)) {
                                addSongToPlaylist(existing.id, songId)
                            }
                        }
                    }
                }
                _snackbarMessage.emit("Import successful! ${backupData.songs.size} songs restored.")
            } else {
                _snackbarMessage.emit("Failed to import backup")
            }
        }
    }

    // Playlist Methods
    fun createPlaylist(name: String) {
        playlistRepository.createPlaylist(name)
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistRepository.addSongToPlaylist(playlistId, songId)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistRepository.removeSongFromPlaylist(playlistId, songId)
    }

    fun deletePlaylist(playlistId: String) {
        playlistRepository.deletePlaylist(playlistId)
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        playlistRepository.renamePlaylist(playlistId, newName)
    }

    fun reorderPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        playlistRepository.reorderSongsInPlaylist(playlistId, fromIndex, toIndex)
    }

    fun getSongsInPlaylist(playlist: Playlist): List<Song> {
        val allSongs = repository.getSongs()
        return playlist.songIds.mapNotNull { id -> allSongs.find { it.id == id } }
    }

    // Settings Setters
    fun setSpecialName(name: String) {
        _specialName.value = name
        storage?.setString("special_name", name)
    }

    fun setSpecialDate(month: Int, day: Int) {
        _specialMonth.value = month
        _specialDay.value = day
        storage?.setString("special_month", month.toString())
        storage?.setString("special_day", day.toString())
    }

    fun setGaplessPlayback(enabled: Boolean) {
        _gaplessPlayback.value = enabled
        storage?.setString("gapless_playback", enabled.toString())
    }

    fun setThemeMode(name: String) {
        _themeMode.value = name
        storage?.setString("theme_mode", name)
    }

    fun scanForMusic(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _snackbarMessage.emit("Scanning for new music...")
            // The actual scanning happens in MainActivity/Platform side and passed via initialSongs or loadSongs
            // But we can trigger a mock delay or emit a message
            onComplete()
        }
    }

    fun restoreDeletedSongs() {
        repository.resetDeletedSongs()
        viewModelScope.launch {
            _snackbarMessage.emit("Library reset. Please scan for music to restore songs.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer?.release()
        httpClient.close()
    }
}
