package com.coderj45.jaysangmusicplayer.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel
import com.coderj45.jaysangmusicplayer.ui.components.SongArt
import com.coderj45.jaysangmusicplayer.ui.components.SongItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onSongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playlistState by viewModel.playlists.collectAsState()
    val currentPlaylist = remember(playlistState) { playlistState.find { it.id == playlist.id } ?: playlist }
    val songs = remember(currentPlaylist) { viewModel.getSongsInPlaylist(currentPlaylist) }
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val isPlaylistActive = currentSong != null && songs.any { it.id == currentSong?.id }
    val isThisPlaylistPlaying = isPlaying && isPlaylistActive

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    
    // Drag and Drop state
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(0f) }

    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(
                    listOf(Color(0xFF282828), MaterialTheme.colorScheme.background)
                ))
        ) {
            // Re-designed Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                // Background Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Album art for the playlist
                    SongArt(
                        imageUrl = if (songs.isNotEmpty()) songs.first().albumArtUrl else null,
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer { 
                                shadowElevation = 20f
                            },
                        cornerRadius = 12,
                        seed = if (songs.isNotEmpty()) songs.first().id else currentPlaylist.id
                    )
              Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentPlaylist.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "JaySang Music • ${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                // Playlist More Options (Top Right)
                var showPlaylistMenu by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)) {
                    IconButton(onClick = { showPlaylistMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(expanded = showPlaylistMenu, onDismissRequest = { showPlaylistMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Rename Playlist") },
                            onClick = {
                                showPlaylistMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = Color.Red) },
                            onClick = {
                                showPlaylistMenu = false
                                viewModel.deletePlaylist(currentPlaylist.id)
                                onBack()
                            }
                        )
                    }
                }

                // Aesthetic Play Button (Overlapping at the bottom right)
                Surface(
                    onClick = { 
                        if (isPlaylistActive) {
                            viewModel.togglePlayPause()
                        } else if (songs.isNotEmpty()) {
                            viewModel.selectSong(songs.first(), songs, currentPlaylist.name) 
                        }
                    },
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(56.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = 28.dp) // Half of the size to overlap
                        .graphicsLayer { shadowElevation = 15f }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isThisPlaylistPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                            contentDescription = if (isThisPlaylistPlaying) "Pause" else "Play", 
                            tint = Color.Black, 
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp)) // Space for the overlapping button

            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No songs in this playlist", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(songs) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { item ->
                                            offset.y.toInt() in item.offset..(item.offset + item.size)
                                        }
                                        ?.also { item ->
                                            draggedItemIndex = item.index
                                        }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggingOffset += dragAmount.y
                                    
                                    val currentDraggedIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                                    
                                    // Simple swap logic
                                    val threshold = 50f
                                    if (draggingOffset > threshold && currentDraggedIndex < songs.size - 1) {
                                        viewModel.reorderPlaylist(currentPlaylist.id, currentDraggedIndex, currentDraggedIndex + 1)
                                        draggedItemIndex = currentDraggedIndex + 1
                                        draggingOffset = 0f
                                    } else if (draggingOffset < -threshold && currentDraggedIndex > 0) {
                                        viewModel.reorderPlaylist(currentPlaylist.id, currentDraggedIndex, currentDraggedIndex - 1)
                                        draggedItemIndex = currentDraggedIndex - 1
                                        draggingOffset = 0f
                                    }
                                },
                                onDragEnd = {
                                    draggedItemIndex = null
                                    draggingOffset = 0f
                                },
                                onDragCancel = {
                                    draggedItemIndex = null
                                    draggingOffset = 0f
                                }
                            )
                        }
                ) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        val isDragging = draggedItemIndex == index
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging) 1.05f else 1f,
                            animationSpec = tween(durationMillis = 100)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    translationY = if (isDragging) draggingOffset else 0f
                                    scaleX = scale
                                    scaleY = scale
                                    shadowElevation = if (isDragging) 8f else 0f
                                }
                                .zIndex(if (isDragging) 1f else 0f)
                                .background(if (isDragging) Color.Gray.copy(alpha = 0.2f) else Color.Transparent)
                        ) {
                            var showSongMenu by remember { mutableStateOf(false) }

                            SongItem(
                                song = song,
                                isCurrentlyPlaying = song.id == currentSong?.id,
                                isPlaying = isPlaying,
                                onClick = {
                                    viewModel.selectSong(song, songs, currentPlaylist.name)
                                    onSongClick()
                                }
                            )

                            IconButton(
                                onClick = { showSongMenu = true },
                                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 48.dp)
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Song Menu", tint = Color.Gray)
                            }

                            DropdownMenu(
                                expanded = showSongMenu,
                                onDismissRequest = { showSongMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from Playlist", color = Color.Red) },
                                    onClick = {
                                        showSongMenu = false
                                        viewModel.removeSongFromPlaylist(currentPlaylist.id, song.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showRenameDialog) {
            var newName by remember { mutableStateOf(currentPlaylist.name) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Playlist") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.renamePlaylist(currentPlaylist.id, newName)
                            showRenameDialog = false
                        }
                    }) { Text("Rename") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
