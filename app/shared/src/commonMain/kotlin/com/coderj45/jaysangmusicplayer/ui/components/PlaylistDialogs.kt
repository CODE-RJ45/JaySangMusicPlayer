package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel

@Composable
fun EditSongInfoDialog(
    song: Song,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Song Info") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, artist) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Playlist Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit
) {
    val playlists by viewModel.playlists.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist") },
        text = {
            if (playlists.isEmpty()) {
                Text("You don't have any playlists yet.")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            modifier = Modifier.clickable { onPlaylistSelected(playlist) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
