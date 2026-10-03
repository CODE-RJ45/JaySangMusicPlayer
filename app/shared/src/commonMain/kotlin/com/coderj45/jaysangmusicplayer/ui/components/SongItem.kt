package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentlyPlaying: Boolean = false,
    isPlaying: Boolean = false // New parameter to control equalizer animation
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art Thumbnail
        SongArt(
            imageUrl = song.albumArtUrl,
            modifier = Modifier.size(48.dp),
            cornerRadius = 4,
            seed = song.id
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
        }

        if (isCurrentlyPlaying) {
             if (isPlaying) {
                 EqualizerAnimation(modifier = Modifier.padding(end = 12.dp))
             } else {
                 Icon(
                     imageVector = Icons.Default.MusicNote,
                     contentDescription = null,
                     tint = MaterialTheme.colorScheme.primary,
                     modifier = Modifier.size(16.dp).padding(end = 8.dp)
                 )
             }
        }

        Text(
            text = formatDuration(song.duration),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun EqualizerAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    
    @Composable
    fun Bar(duration: Int) {
        val height by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(height)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
        )
    }

    Row(
        modifier = modifier.height(14.dp).width(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Bar(400)
        Bar(600)
        Bar(500)
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}
