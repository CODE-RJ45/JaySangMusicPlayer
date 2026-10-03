package com.coderj45.jaysangmusicplayer.data

import com.coderj45.jaysangmusicplayer.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json

/**
 * Repository for managing songs. 
 * Now focused on local storage/memory for local-only mode.
 */
class SongRepository(private val storage: Storage? = null) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _localSongs = MutableStateFlow<List<Song>>(emptyList())
    val localSongs: StateFlow<List<Song>> = _localSongs.asStateFlow()

    private var persistedSongs: Map<String, Song> = loadPersistedSongs().associateBy { it.id }
    private var deletedSongIds: MutableSet<String> = loadDeletedSongIds().toMutableSet()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    init {
        // Initial load of recently played needs persistedSongs to be populated
        _recentlyPlayed.value = loadRecentlyPlayed()
    }

    fun updateSongs(songs: List<Song>) {
        // Filter out songs that have been marked as deleted
        val filteredSongs = songs.filter { !deletedSongIds.contains(it.id) }
        
        // Merge incoming songs with persisted data (notes, custom art)
        val mergedSongs = filteredSongs.map { song ->
            persistedSongs[song.id]?.let { persisted ->
                song.copy(
                    title = persisted.title, // Keep edited titles
                    artist = persisted.artist, // Keep edited artists
                    note = persisted.note ?: song.note,
                    lyrics = persisted.lyrics ?: song.lyrics,
                    albumArtUrl = persisted.albumArtUrl ?: song.albumArtUrl
                )
            } ?: song
        }
        _localSongs.value = mergedSongs
        saveSongs(mergedSongs)
        
        // Also update recently played with merged data if they exist there
        _recentlyPlayed.update { current ->
            current.asSequence().map { song ->
                mergedSongs.find { it.id == song.id } ?: song
            }.filter { !deletedSongIds.contains(it.id) }.toList()
        }
    }

    /**
     * Updates a song and ensures it's persisted immediately.
     */
    fun updateSong(updatedSong: Song) {
        val currentList = _localSongs.value.map { 
            if (it.id == updatedSong.id) updatedSong else it 
        }
        _localSongs.value = currentList
        saveSongs(currentList)

        // Update in recently played too
        _recentlyPlayed.update { current ->
            current.map { if (it.id == updatedSong.id) updatedSong else it }
        }
    }

    fun deleteSong(songId: String) {
        // Add to permanent deleted list
        deletedSongIds.add(songId)
        saveDeletedSongIds(deletedSongIds.toList())

        val currentList = _localSongs.value.filter { it.id != songId }
        _localSongs.value = currentList
        saveSongs(currentList)

        // Remove from recently played too
        _recentlyPlayed.update { current ->
            current.filter { it.id != songId }
        }
    }

    fun resetDeletedSongs() {
        deletedSongIds.clear()
        saveDeletedSongIds(emptyList())
    }

    fun addToRecentlyPlayed(song: Song) {
        _recentlyPlayed.update { currentList ->
            val newList = (listOf(song) + currentList.filter { it.id != song.id }).take(10)
            saveRecentlyPlayed(newList)
            newList
        }
    }

    fun getSongs(): List<Song> {
        return _localSongs.value
    }

    private fun saveSongs(songs: List<Song>) {
        // Only save songs that have custom data to save space, 
        // or just save everything. Let's save everything for simplicity.
        storage?.setString("local_songs", json.encodeToString(songs))
        persistedSongs = songs.associateBy { it.id }
    }

    private fun loadPersistedSongs(): List<Song> {
        val saved = storage?.getString("local_songs") ?: return emptyList()
        return try {
            json.decodeFromString(saved)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveRecentlyPlayed(songs: List<Song>) {
        storage?.setString("recently_played", json.encodeToString(songs.map { it.id }))
    }

    private fun loadRecentlyPlayed(): List<Song> {
        val savedIds = storage?.getString("recently_played") ?: return emptyList()
        return try {
            val ids: List<String> = json.decodeFromString(savedIds)
            ids.mapNotNull { id -> persistedSongs[id] }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveDeletedSongIds(ids: List<String>) {
        storage?.setString("deleted_song_ids", json.encodeToString(ids))
    }

    private fun loadDeletedSongIds(): List<String> {
        val saved = storage?.getString("deleted_song_ids") ?: return emptyList()
        return try {
            json.decodeFromString(saved)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
