package com.coderj45.jaysangmusicplayer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel
import com.coderj45.jaysangmusicplayer.ui.screens.LibraryScreen
import com.coderj45.jaysangmusicplayer.ui.screens.PlayerScreen
import com.coderj45.jaysangmusicplayer.ui.screens.PlaylistDetailScreen
import com.coderj45.jaysangmusicplayer.ui.screens.HomeScreen
import com.coderj45.jaysangmusicplayer.ui.screens.SearchScreen
import com.coderj45.jaysangmusicplayer.ui.screens.SplashScreen
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.Playlist
import com.coderj45.jaysangmusicplayer.ui.theme.SpotifyTheme
import com.coderj45.jaysangmusicplayer.ui.components.MiniPlayer
import com.coderj45.jaysangmusicplayer.data.Storage
import com.coderj45.jaysangmusicplayer.ui.ShareManager
import com.coderj45.jaysangmusicplayer.player.AudioPlayer

enum class Screen {
    Home,
    Search,
    Library,
    Player,
    PlaylistDetail,
    Settings
}

@Composable
fun App(
    audioPlayer: AudioPlayer? = null,
    storage: Storage? = null,
    shareManager: ShareManager? = null,
    initialSongs: List<Song> = emptyList(),
    onImportBackup: (() -> Unit)? = null,
    importedBackupUri: String? = null,
    onImportConsumed: () -> Unit = {},
    onRefreshSongs: () -> Unit = {},
    openPlayerInitially: Boolean = false,
    onOpenPlayerConsumed: () -> Unit = {}
) {
    SpotifyTheme {
        val viewModel: MusicViewModel = viewModel { 
            MusicViewModel(audioPlayer, storage, shareManager, initialSongs = initialSongs) 
        }
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(openPlayerInitially) {
            if (openPlayerInitially) {
                showSplash = false
            }
        }

        if (showSplash) {
            SplashScreen(onFinish = { showSplash = false })
        } else {
            var currentScreen by remember { mutableStateOf(if (openPlayerInitially) Screen.Player else Screen.Home) }
            
            LaunchedEffect(openPlayerInitially) {
                if (openPlayerInitially) {
                    currentScreen = Screen.Player
                    onOpenPlayerConsumed()
                }
            }

            var previousScreen by remember { mutableStateOf(Screen.Home) }
            var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
            
            val currentSong by viewModel.currentSong.collectAsState()
            val isPlaying by viewModel.isPlaying.collectAsState()
            val playbackPosition by viewModel.playbackPosition.collectAsState()

            LaunchedEffect(initialSongs) {
                viewModel.loadSongs(initialSongs.toList())
            }

            LaunchedEffect(importedBackupUri) {
                importedBackupUri?.let {
                    viewModel.importBackup(it)
                    onImportConsumed()
                }
            }

            Scaffold(
                bottomBar = {
                    if (currentScreen != Screen.Player) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Column {
                                if (currentSong != null) {
                                    val duration = currentSong?.duration?.coerceAtLeast(1L) ?: 1L
                                    val progress = (playbackPosition.toFloat() / duration).coerceIn(0f, 1f)
                                    
                                    MiniPlayer(
                                        song = currentSong,
                                        isPlaying = isPlaying,
                                        progress = progress,
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNext = { viewModel.playNext() },
                                        onPrevious = { viewModel.playPrevious() },
                                        onClick = { currentScreen = Screen.Player }
                                    )
                                }
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ) {
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                if (currentScreen == Screen.Home) Icons.Default.Home else Icons.Outlined.Home, 
                                                contentDescription = "Home"
                                            ) 
                                        },
                                        label = { Text("Home") },
                                        selected = currentScreen == Screen.Home,
                                        onClick = { 
                                            previousScreen = Screen.Home
                                            currentScreen = Screen.Home 
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            unselectedIconColor = Color.Gray,
                                            selectedTextColor = Color.White,
                                            unselectedTextColor = Color.Gray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                if (currentScreen == Screen.Search) Icons.Default.Search else Icons.Outlined.Search, 
                                                contentDescription = "Search"
                                            ) 
                                        },
                                        label = { Text("Search") },
                                        selected = currentScreen == Screen.Search,
                                        onClick = { 
                                            previousScreen = Screen.Search
                                            currentScreen = Screen.Search 
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            unselectedIconColor = Color.Gray,
                                            selectedTextColor = Color.White,
                                            unselectedTextColor = Color.Gray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                if (currentScreen == Screen.Library || currentScreen == Screen.PlaylistDetail) 
                                                    Icons.Default.LibraryMusic else Icons.Outlined.LibraryMusic, 
                                                contentDescription = "Library"
                                            ) 
                                        },
                                        label = { Text("Library") },
                                        selected = currentScreen == Screen.Library || currentScreen == Screen.PlaylistDetail,
                                        onClick = { 
                                            previousScreen = Screen.Library
                                            currentScreen = Screen.Library 
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            unselectedIconColor = Color.Gray,
                                            selectedTextColor = Color.White,
                                            unselectedTextColor = Color.Gray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Surface(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Home -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onSongClick = { 
                                    previousScreen = Screen.Home
                                    currentScreen = Screen.Player 
                                },
                                onPlaylistClick = { playlist ->
                                    selectedPlaylist = playlist
                                    previousScreen = Screen.Home
                                    currentScreen = Screen.PlaylistDetail
                                },
                                onSettingsClick = {
                                    previousScreen = Screen.Home
                                    currentScreen = Screen.Settings
                                }
                            )
                        }
                        Screen.Search -> {
                            SearchScreen(
                                viewModel = viewModel,
                                onSongClick = { 
                                    previousScreen = Screen.Search
                                    currentScreen = Screen.Player 
                                }
                            )
                        }
                        Screen.Library -> {
                            LibraryScreen(
                                viewModel = viewModel,
                                onSongClick = { 
                                    previousScreen = Screen.Library
                                    currentScreen = Screen.Player 
                                },
                                onPlaylistClick = { playlist ->
                                    selectedPlaylist = playlist
                                    previousScreen = Screen.Library
                                    currentScreen = Screen.PlaylistDetail
                                },
                                onImportClick = { onImportBackup?.invoke() },
                                onScan = onRefreshSongs
                            )
                        }
                        Screen.Player -> {
                            PlayerScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = previousScreen }
                            )
                        }
                        Screen.PlaylistDetail -> {
                            selectedPlaylist?.let { playlist ->
                                PlaylistDetailScreen(
                                    playlist = playlist,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = previousScreen },
                                    onSongClick = { 
                                        previousScreen = Screen.PlaylistDetail
                                        currentScreen = Screen.Player 
                                    }
                                )
                            } ?: run {
                                currentScreen = Screen.Library
                            }
                        }
                        Screen.Settings -> {
                            com.coderj45.jaysangmusicplayer.ui.screens.SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentScreen = previousScreen },
                                onImportClick = { onImportBackup?.invoke() }
                            )
                        }
                    }
                }
            }
        }
    }
}
