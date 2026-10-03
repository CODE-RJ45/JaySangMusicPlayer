package com.coderj45.jaysangmusicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel
import com.coderj45.jaysangmusicplayer.ui.components.FlippableAlbumArt
import com.coderj45.jaysangmusicplayer.ui.components.PlaylistThumbnail

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val allSongs by viewModel.songs.collectAsState()
    val specialName by viewModel.specialName.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    val greeting = "Good Morning $specialName" // Simplified for build

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF282828).copy(alpha = 0.8f), 
                        Color(0xFF121212)
                    ),
                    startY = 0f,
                    endY = 500f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        // Enhanced App Logo Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            // High-end Logo Design
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(MaterialTheme.colorScheme.primary, Color(0xFF1DB954))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column {
                                Text(
                                    text = "JAYSANG",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "MUSIC PLAYER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = onSettingsClick) {
                                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                                }
                            }
                        }
                    }
                }

                // Top Grid Section (Quick access)
                item {
                    val quickAccessItems = playlists.take(6)
                    if (quickAccessItems.isNotEmpty()) {
                        Box(modifier = Modifier.height(200.dp).padding(horizontal = 16.dp)) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(quickAccessItems) { playlist ->
                                    val playlistSongs = remember(playlist, allSongs) { 
                                        viewModel.getSongsInPlaylist(playlist) 
                                    }
                                    QuickAccessCard(
                                        playlist = playlist,
                                        songs = playlistSongs,
                                        onClick = { onPlaylistClick(playlist) }
                                    )
                                }
                            }
                        }
                    }
                }


                // Recently Played Section
                if (recentlyPlayed.isNotEmpty()) {
                    item {
                        SectionHeader("Recently Played")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(recentlyPlayed) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        viewModel.selectSong(song, recentlyPlayed, "Recently Played")
                                        onSongClick()
                                    }
                                )
                            }
                        }
                    }
                }

                // Playlists Section
                if (playlists.isNotEmpty()) {
                    item {
                        SectionHeader("Your Playlists")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(playlists) { playlist ->
                                val playlistSongs = remember(playlist, allSongs) { 
                                    viewModel.getSongsInPlaylist(playlist) 
                                }
                                PlaylistCard(
                                    playlist = playlist,
                                    songs = playlistSongs,
                                    onClick = { onPlaylistClick(playlist) }
                                )
                            }
                        }
                    }
                }

                // Made for You Section
                if (allSongs.isNotEmpty()) {
                    item {
                        SectionHeader("Made for you")
                        val madeForYouSongs = remember(allSongs) { allSongs.shuffled().take(10) }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(madeForYouSongs) { song ->
                                SongCard(
                                    song = song,
                                    onClick = {
                                        viewModel.selectSong(song, madeForYouSongs, "Made For You")
                                        onSongClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 12.dp)
    )
}

@Composable
fun QuickAccessCard(playlist: Playlist, songs: List<Song>, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF2A2A2A).copy(alpha = 0.8f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlaylistThumbnail(
                songs = songs,
                modifier = Modifier.size(56.dp),
                placeholderName = playlist.name
            )
            Text(
                text = playlist.name,
                modifier = Modifier.padding(horizontal = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        FlippableAlbumArt(
            song = song,
            modifier = Modifier
                .size(150.dp),
            cornerRadius = 12
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFB3B3B3),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, songs: List<Song>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        PlaylistThumbnail(
            songs = songs,
            modifier = Modifier.size(150.dp),
            placeholderName = playlist.name
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
