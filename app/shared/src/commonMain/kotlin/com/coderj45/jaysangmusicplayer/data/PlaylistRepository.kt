package com.coderj45.jaysangmusicplayer.data

import com.coderj45.jaysangmusicplayer.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlaylistRepository(private val storage: Storage? = null) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _playlists = MutableStateFlow<List<Playlist>>(loadPlaylists())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    fun createPlaylist(name: String) {
        val newPlaylist = Playlist(
            id = "playlist_${_playlists.value.size}_${name.hashCode()}",
            name = name
        )
        _playlists.update { it + newPlaylist }
        savePlaylists(_playlists.value)
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        _playlists.update { currentPlaylists ->
            currentPlaylists.map { playlist ->
                if (playlist.id == playlistId && !playlist.songIds.contains(songId)) {
                    playlist.copy(songIds = playlist.songIds + songId)
                } else {
                    playlist
                }
            }
        }
        savePlaylists(_playlists.value)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        _playlists.update { currentPlaylists ->
            currentPlaylists.map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(songIds = playlist.songIds - songId)
                } else {
                    playlist
                }
            }
        }
        savePlaylists(_playlists.value)
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        _playlists.update { currentPlaylists ->
            currentPlaylists.map { playlist ->
                if (playlist.id == playlistId) {
                    playlist.copy(name = newName)
                } else {
                    playlist
                }
            }
        }
        savePlaylists(_playlists.value)
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.update { it.filter { p -> p.id != playlistId } }
        savePlaylists(_playlists.value)
    }

    fun reorderSongsInPlaylist(playlistId: String, fromIndex: Int, toIndex: Int) {
        _playlists.update { currentPlaylists ->
            currentPlaylists.map { playlist ->
                if (playlist.id == playlistId) {
                    val newList = playlist.songIds.toMutableList()
                    if (fromIndex in newList.indices && toIndex in newList.indices) {
                        val item = newList.removeAt(fromIndex)
                        newList.add(toIndex, item)
                        playlist.copy(songIds = newList)
                    } else {
                        playlist
                    }
                } else {
                    playlist
                }
            }
        }
        savePlaylists(_playlists.value)
    }

    private fun savePlaylists(playlists: List<Playlist>) {
        storage?.setString("playlists", json.encodeToString(playlists))
    }

    private fun loadPlaylists(): List<Playlist> {
        val saved = storage?.getString("playlists") ?: return emptyList()
        return try {
            json.decodeFromString(saved)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
