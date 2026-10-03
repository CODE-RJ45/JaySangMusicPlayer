package com.coderj45.jaysangmusicplayer.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.parseLrc
import com.coderj45.jaysangmusicplayer.ui.MusicViewModel
import com.coderj45.jaysangmusicplayer.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSource by viewModel.currentSource.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val isShuffleEnabled by viewModel.isShuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    
    var showMenu by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showEditInfoDialog by remember { mutableStateOf(false) }
    var showEditNoteDialog by remember { mutableStateOf(false) }
    var showEditLyricsDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showLyricsOverlay by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val imagePickerLauncher = rememberImagePickerLauncher { uri ->
        currentSong?.let { viewModel.updateAlbumArt(it.id, uri) }
    }

    // Dynamic background color based on song
    val dominantColor by animateColorAsState(
        targetValue = remember(currentSong) {
            if (currentSong == null) Color(0xFF535353)
            else {
                // Simple stable color generation from ID
                val hash = currentSong!!.id.hashCode()
                val r = (hash and 0xFF0000 shr 16) % 100 + 40
                val g = (hash and 0x00FF00 shr 8) % 100 + 40
                val b = (hash and 0x0000FF) % 100 + 40
                Color(r, g, b)
            }
        },
        animationSpec = tween(1000)
    )

    val gradientColors = listOf(
        dominantColor,
        MaterialTheme.colorScheme.background
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        // Gesture logic handled during drag
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 50) { // Significant swipe down
                            onBack()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", modifier = Modifier.size(32.dp), tint = Color.White)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentSource.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Album Art") },
                            onClick = { 
                                showMenu = false
                                imagePickerLauncher() 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Playlist") },
                            onClick = { 
                                showMenu = false
                                showAddToPlaylistDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Song Info") },
                            onClick = { 
                                showMenu = false
                                showEditInfoDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add Note") },
                            onClick = { 
                                showMenu = false
                                showEditNoteDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Lyrics") },
                            onClick = { 
                                showMenu = false
                                showEditLyricsDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save Story to Gallery") },
                            onClick = { 
                                showMenu = false
                                viewModel.saveStoryToGallery()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Song Info") },
                            onClick = { 
                                showMenu = false
                                showDetailsDialog = true
                            }
                        )
                    }
                }
            }

            // Synced Lyric Display above Album Art
            val lyricLines = remember(currentSong?.lyrics) { 
                parseLrc(currentSong?.lyrics) 
            }
      val activeLyric = remember(playbackPosition, lyricLines) {
                lyricLines.findLast { it.timeMs <= playbackPosition }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activeLyric,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(600)) + slideInVertically { it / 2 }).togetherWith(
                            fadeOut(animationSpec = tween(600)) + slideOutVertically { -it / 2 }
                        )
                    },
                    label = "LyricTransition"
                ) { lyric ->
                    if (lyric != null && lyric.text.isNotBlank()) {
                        Text(
                            text = lyric.text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                letterSpacing = 0.2.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            maxLines = 2
                        )
                    } else {
                        // Empty space or placeholder when no lyric is active
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            // Flippable Album Art
            FlippableAlbumArt(
                song = currentSong,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .graphicsLayer {
                        translationX = offsetX
                        alpha = 1f - (kotlin.math.abs(offsetX) / 1000f).coerceIn(0f, 0.5f)
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 150) {
                                    viewModel.playPrevious()
                                } else if (offsetX < -150) {
                                    viewModel.playNext()
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                            }
                        )
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Song Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong?.title ?: "Select a song",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = currentSong?.artist ?: "Unknown Artist",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
                IconButton(
                    onClick = { showLyricsOverlay = true },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Notes,
                        contentDescription = "Lyrics",
                        tint = if (currentSong?.lyrics.isNullOrBlank()) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar
            val duration = currentSong?.duration?.coerceAtLeast(1L) ?: 1L
            var sliderPosition by remember(playbackPosition) { mutableStateOf(playbackPosition.toFloat() / duration) }
            var isDragging by remember { mutableStateOf(false) }
            
            Slider(
                value = if (isDragging) sliderPosition else (playbackPosition.toFloat() / duration).coerceIn(0f, 1f),
                onValueChange = { 
                    isDragging = true
                    sliderPosition = it 
                },
                onValueChangeFinished = {
                    isDragging = false
                    viewModel.seekTo((sliderPosition * duration).toLong())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(playbackPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = formatDuration(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PlayerControls(
                isPlaying = isPlaying,
                onTogglePlayPause = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.togglePlayPause() 
                },
                onNext = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.playNext() 
                },
                onPrevious = { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.playPrevious() 
                },
                isShuffleEnabled = isShuffleEnabled,
                onToggleShuffle = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleShuffle() 
                },
                repeatMode = repeatMode,
                onToggleRepeat = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleRepeatMode() 
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )

        if (showAddToPlaylistDialog && currentSong != null) {
            AddToPlaylistDialog(
                viewModel = viewModel,
                onDismiss = { showAddToPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, currentSong!!.id)
                    showAddToPlaylistDialog = false
                }
            )
        }

        if (showEditInfoDialog && currentSong != null) {
            EditSongInfoDialog(
                song = currentSong!!,
                onDismiss = { showEditInfoDialog = false },
                onSave = { title, artist ->
                    viewModel.updateSongInfo(currentSong!!.id, title, artist)
                    showEditInfoDialog = false
                }
            )
        }
        if (showEditNoteDialog && currentSong != null) {
            EditNoteDialog(
                currentNote = currentSong?.note ?: "",
                onDismiss = { showEditNoteDialog = false },
                onSave = { note ->
                    viewModel.updateSongNote(currentSong!!.id, note)
                    showEditNoteDialog = false
                }
            )
        }
        if (showDetailsDialog && currentSong != null) {
            SongDetailsDialog(
                song = currentSong!!,
                onDismiss = { showDetailsDialog = false }
            )
        }

        if (showEditLyricsDialog && currentSong != null) {
            EditLyricsDialog(
                song = currentSong!!,
                viewModel = viewModel,
                onDismiss = { showEditLyricsDialog = false },
                onSave = { lyrics ->
                    viewModel.updateSongLyrics(currentSong!!.id, lyrics)
                    showEditLyricsDialog = false
                }
            )
        }

        if (showLyricsOverlay && currentSong != null) {
            LyricsOverlay(
                song = currentSong!!,
                playbackPosition = playbackPosition,
                onDismiss = { showLyricsOverlay = false },
                backgroundColor = dominantColor
            )
        }
    }
}

@Composable
fun LyricsOverlay(
    song: Song,
    playbackPosition: Long,
    onDismiss: () -> Unit,
    backgroundColor: Color
) {
    val lyricLines = remember(song.lyrics) { parseLrc(song.lyrics) }
    val listState = rememberLazyListState()
    
    // Find active lyric index
    val activeIndex = remember(playbackPosition, lyricLines) {
        val index = lyricLines.indexOfLast { it.timeMs <= playbackPosition }
        if (index == -1 && lyricLines.isNotEmpty()) 0 else index
    }

    // Auto-scroll logic
    LaunchedEffect(activeIndex) {
        if (activeIndex != -1) {
            listState.animateScrollToItem(
                (activeIndex - 2).coerceAtLeast(0) // Keep current line slightly above center
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (lyricLines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No lyrics available for this song.\nTap the menu to add some in LRC format!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(vertical = 100.dp) // Large padding for center focus
                ) {
                    itemsIndexed(lyricLines) { index, line ->
                        val isActive = index == activeIndex
                        val color by animateColorAsState(
                            targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.3f),
                            animationSpec = tween(300)
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.1f else 1f,
                            animationSpec = tween(300)
                        )

                        Text(
                            text = line.text,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                lineHeight = 44.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold
                            ),
                            color = color,
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clickable { 
                                    // Optional: Tap to seek logic could go here
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditLyricsDialog(
    song: Song,
    viewModel: MusicViewModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var lyrics by remember { mutableStateOf(song.lyrics ?: "") }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Lyrics")
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = {
                        scope.launch {
                            isSearching = true
                            val result = viewModel.fetchLyricsOnline(song.title, song.artist)
                            if (result != null) {
                                lyrics = result
                            }
                            isSearching = false
                        }
                    }) {
                        Text("Search Online", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        },
        text = {
            OutlinedTextField(
                value = lyrics,
                onValueChange = { lyrics = it },
                placeholder = { Text("Paste LRC format here, e.g:\n[00:12.34] Hello World") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(onClick = { onSave(lyrics) }) {
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
fun SongDetailsDialog(song: Song, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Song Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Title", song.title)
                DetailRow("Artist", song.artist)
                DetailRow("Duration", formatDuration(song.duration))
                song.mediaUri?.let { DetailRow("Path", it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EditNoteDialog(
    currentNote: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember { mutableStateOf(currentNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Personal Note", 
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Capture a thought, a lyric, or a memory...") },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This note is saved specifically for this song.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(note) },
                shape = RoundedCornerShape(50)
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
