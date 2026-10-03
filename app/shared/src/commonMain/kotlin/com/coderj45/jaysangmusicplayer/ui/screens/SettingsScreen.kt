package com.coderj45.jaysangmusicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onImportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val gaplessPlayback by viewModel.gaplessPlayback.collectAsState()
    
    var showDedicationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. Audio & Playback
            item { SettingHeader("Audio & Playback") }
            item {
                SettingToggleItem(
                    title = "Gapless Playback",
                    subtitle = "Eliminate silence between tracks",
                    checked = gaplessPlayback,
                    onCheckedChange = { viewModel.setGaplessPlayback(it) },
                    icon = Icons.Default.GraphicEq
                )
            }
            item {
                SettingClickItem(
                    title = "Equalizer",
                    subtitle = "Adjust audio frequencies",
                    onClick = { /* Mock */ },
                    icon = Icons.Default.MusicNote
                )
            }

            // 2. Library & Storage
            item { SettingHeader("Library & Storage") }
            item {
                SettingClickItem(
                    title = "Scan for Music",
                    subtitle = "Refresh your local music library",
                    onClick = { viewModel.scanForMusic() },
                    icon = Icons.Default.Storage
                )
            }
            item {
                SettingClickItem(
                    title = "Restore Deleted Songs",
                    subtitle = "Undo all permanent removals",
                    onClick = { viewModel.restoreDeletedSongs() },
                    icon = Icons.Default.Refresh
                )
            }

            // 3. Backup & Data
            item { SettingHeader("Backup & Data") }
            item {
                SettingClickItem(
                    title = "Export Full Package",
                    subtitle = "Bundle songs, notes, and playlists",
                    onClick = { viewModel.exportBackup() },
                    icon = Icons.Default.Backup
                )
            }
            item {
                SettingClickItem(
                    title = "Import Full Package",
                    subtitle = "Restore data from a backup file",
                    onClick = onImportClick,
                    icon = Icons.Default.Backup
                )
            }

            // 4. About & Info
            item { SettingHeader("About") }
            item {
                SettingClickItem(
                    title = "Dedicated To",
                    subtitle = "Rosa Alma S. Buendia",
                    onClick = { showDedicationDialog = true },
                    icon = Icons.Default.Favorite
                )
            }
            item {
                SettingClickItem(
                    title = "Developer",
                    subtitle = "Ar-jay F. Llanera",
                    onClick = {},
                    icon = Icons.Default.Person
                )
            }
            item {
                SettingClickItem(
                    title = "Version",
                    subtitle = "1.0.0 (Stable)",
                    onClick = {},
                    icon = Icons.Default.Info
                )
            }
        }
    }

    if (showDedicationDialog) {
        AlertDialog(
            onDismissRequest = { showDedicationDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("A Special Dedication") 
                }
            },
            text = {
                Text(
                    text = "This application is dedicated to my Love, Rosa Alma S. Buendia. Thank you for being my inspiration and the melody to my life's song. I love you!",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            },
            confirmButton = {
                TextButton(onClick = { showDedicationDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SettingHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingClickItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = Color.Gray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.White) },
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = Color.Gray) },
        leadingContent = { Icon(icon, contentDescription = null, tint = Color.White) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
