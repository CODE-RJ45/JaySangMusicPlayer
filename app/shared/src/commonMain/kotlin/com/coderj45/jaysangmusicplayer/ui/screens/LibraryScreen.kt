package com.coderj45.jaysangmusicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel
import com.coderj45.jaysangmusicplayer.ui.SortOrder
import com.coderj45.jaysangmusicplayer.ui.components.AddToPlaylistDialog
import com.coderj45.jaysangmusicplayer.ui.components.CreatePlaylistDialog
import com.coderj45.jaysangmusicplayer.ui.components.EditSongInfoDialog
import com.coderj45.jaysangmusicplayer.ui.components.PlaylistThumbnail
import com.coderj45.jaysangmusicplayer.ui.components.SongItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LibraryTab {
    SONGS, PLAYLISTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onSongClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onImportClick: () -> Unit = {},
    onScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showLibraryMenu by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF8D67AB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("L", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Row {
                IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                    Icon(
                        if (isSearchVisible) Icons.Default.Close else Icons.Default.Search, 
                        contentDescription = "Search Library"
                    )
                }
                IconButton(onClick = { showCreatePlaylistDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create Playlist")
                }
                Box {
                    IconButton(onClick = { showLibraryMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(
                        expanded = showLibraryMenu,
                        onDismissRequest = { showLibraryMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export Full Package") },
                            onClick = {
                                showLibraryMenu = false
                                viewModel.exportBackup()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Full Package") },
                            onClick = {
                                showLibraryMenu = false
                                onImportClick()
                            }
                        )
                    }
                }
            }
        }

        if (isSearchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search your library") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF282828),
                    unfocusedContainerColor = Color(0xFF282828),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == LibraryTab.PLAYLISTS,
                onClick = { selectedTab = LibraryTab.PLAYLISTS },
                label = { Text("Playlists") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.Black
                ),
                border = null,
                shape = androidx.compose.foundation.shape.CircleShape
            )
            FilterChip(
                selected = selectedTab == LibraryTab.SONGS,
                onClick = { selectedTab = LibraryTab.SONGS },
                label = { Text("Songs") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.Black
                ),
                border = null,
                shape = androidx.compose.foundation.shape.CircleShape
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == LibraryTab.SONGS) {
            SongsList(
                songs = songs,
                sortOrder = sortOrder,
                currentSong = currentSong,
                isPlaying = isPlaying,
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        onScan()
                        delay(1500)
                        isRefreshing = false
                    }
                },
                viewModel = viewModel,
                onSongClick = onSongClick
            )
        } else {
            PlaylistsList(
                playlists = playlists,
                viewModel = viewModel,
                onPlaylistClick = onPlaylistClick
            )
        }
    }

    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name ->
                viewModel.createPlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsList(
    songs: List<Song>,
    sortOrder: SortOrder,
    currentSong: Song?,
    isPlaying: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    viewModel: MusicViewModel,
    onSongClick: () -> Unit
) {
    val showSkeleton = songs.isEmpty() && isRefreshing

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = sortOrder == SortOrder.TITLE,
                onClick = { viewModel.setSortOrder(SortOrder.TITLE) },
                label = { Text("Title") }
            )
            FilterChip(
                selected = sortOrder == SortOrder.ARTIST,
                onClick = { viewModel.setSortOrder(SortOrder.ARTIST) },
                label = { Text("Artist") }
            )
            FilterChip(
                selected = sortOrder == SortOrder.DURATION,
                onClick = { viewModel.setSortOrder(SortOrder.DURATION) },
                label = { Text("Duration") }
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onRefresh) {
                Text("Scan", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (songs.isEmpty() && !showSkeleton) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No music found",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
            }
        } else if (showSkeleton) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(10) {
                    SongItemSkeleton()
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(songs) { song ->
                    Box {
                        var showSongMenu by remember { mutableStateOf(false) }
                        var showAddToPlaylistDialog by remember { mutableStateOf(false) }
                        var showEditInfoDialog by remember { mutableStateOf(false) }

                        SongItem(
                            song = song,
                            isCurrentlyPlaying = song.id == currentSong?.id,
                            isPlaying = isPlaying,
                            onClick = {
                                viewModel.selectSong(song, songs, "Your Library")
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
                                text = { Text("Add to Playlist") },
                                onClick = {
                                    showSongMenu = false
                                    showAddToPlaylistDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Info") },
                                onClick = {
                                    showSongMenu = false
                                    showEditInfoDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Remove from Library", 
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                onClick = {
                                    showSongMenu = false
                                    viewModel.removeSongFromLibrary(song.id)
                                }
                            )
                        }

                        if (showAddToPlaylistDialog) {
                            AddToPlaylistDialog(
                                viewModel = viewModel,
                                onDismiss = { showAddToPlaylistDialog = false },
                                onPlaylistSelected = { playlist ->
                                    viewModel.addSongToPlaylist(playlist.id, song.id)
                                    showAddToPlaylistDialog = false
                                }
                            )
                        }

                        if (showEditInfoDialog) {
                            EditSongInfoDialog(
                                song = song,
                                onDismiss = { showEditInfoDialog = false },
                                onSave = { newTitle, newArtist ->
                                    viewModel.updateSongInfo(song.id, newTitle, newArtist)
                                    showEditInfoDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun PlaylistsList(
    playlists: List<Playlist>,
    viewModel: MusicViewModel,
    onPlaylistClick: (Playlist) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No playlists yet", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(playlists) { playlist ->
                val songs = remember(playlist) { viewModel.getSongsInPlaylist(playlist) }
                ListItem(
                    leadingContent = {
                        PlaylistThumbnail(
                            songs = songs,
                            modifier = Modifier.size(48.dp),
                            placeholderName = playlist.name
                        )
                    },
                    headlineContent = { Text(playlist.name) },
                    supportingContent = { Text("${playlist.songIds.size} songs") },
                    modifier = Modifier.clickable { onPlaylistClick(playlist) }
                )
            }
        }
    }
}
