package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Song

@Composable
fun PlaylistThumbnail(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    placeholderName: String = ""
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A)),
        contentAlignment = Alignment.Center
    ) {
        if (songs.isEmpty()) {
            // Placeholder for empty playlist
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                if (placeholderName.isNotEmpty()) {
                    Text(
                        text = placeholderName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        } else if (songs.size < 4) {
            // Single image if less than 4 songs
            SongArt(
                imageUrl = songs.first().albumArtUrl,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 8,
                seed = songs.first().id
            )
        } else {
      // 2x2 Grid
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.weight(1f)) {
                    ThumbnailCell(songs.getOrNull(0), Modifier.weight(1f))
                    ThumbnailCell(songs.getOrNull(1), Modifier.weight(1f))
                }
                Row(modifier = Modifier.weight(1f)) {
                    ThumbnailCell(songs.getOrNull(2), Modifier.weight(1f))
                    ThumbnailCell(songs.getOrNull(3), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThumbnailCell(song: Song?, modifier: Modifier = Modifier) {
    SongArt(
        imageUrl = song?.albumArtUrl,
        modifier = modifier,
        cornerRadius = 0,
        seed = song?.id ?: ""
    )
}
