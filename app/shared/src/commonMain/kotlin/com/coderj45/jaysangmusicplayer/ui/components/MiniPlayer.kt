package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Song

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: Float, // 0f to 1f
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var offsetX by remember { mutableStateOf(0f) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .graphicsLayer {
                translationX = offsetX
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 100) {
                            onPrevious()
                        } else if (offsetX < -100) {
                            onNext()
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
            .clickable(onClick = onClick),
        color = Color(0xFF282828), // Spotify Dark Gray
        tonalElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art and Info Section (Clickable Area)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = onClick), // Localized click for navigation
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SongArt(
                        imageUrl = song.albumArtUrl,
                        modifier = Modifier.size(44.dp),
                        cornerRadius = 4,
                        seed = song.id
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .basicMarquee(iterations = Int.MAX_VALUE)
                            )
                        }
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Progress Bar at the bottom
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.BottomCenter),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
