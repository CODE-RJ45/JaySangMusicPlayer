package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coderj45.jaysangmusicplayer.ui.RepeatMode

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    isShuffleEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    repeatMode: RepeatMode,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        IconButton(onClick = onToggleShuffle) {
            Icon(
                Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffleEnabled) MaterialTheme.colorScheme.primary else Color.White
            )
        }

        // Previous Button
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp), tint = Color.White)
        }

        // Play/Pause Button
        Surface(
            onClick = onTogglePlayPause,
            shape = androidx.compose.foundation.shape.CircleShape,
            color = Color.White,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Next Button
        IconButton(onClick = onNext) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp), tint = Color.White)
        }

        // Repeat Button
        IconButton(onClick = onToggleRepeat) {
            Icon(
                imageVector = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                contentDescription = "Repeat",
                tint = if (repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else Color.White
            )
        }
    }
}
