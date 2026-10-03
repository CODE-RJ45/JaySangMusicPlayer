package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.coderj45.jaysangmusicplayer.model.Song

@Composable
fun FlippableAlbumArt(
    song: Song?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 12
) {
    var rotated by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { rotated = !rotated },
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            // Front side (Album Art)
            SongArt(
                imageUrl = song?.albumArtUrl,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = cornerRadius,
                seed = song?.id ?: ""
            )
        } else {
      // Back side (Aesthetic Note)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
                    .clip(RoundedCornerShape(cornerRadius.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
                        )
                    )
                    .padding(2.dp)
                    .background(Color.White, RoundedCornerShape((cornerRadius - 2).dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song?.note.let { if (it.isNullOrBlank()) "Write a beautiful note for this song..." else it },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color(0xFF333333),
                            lineHeight = 28.sp,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.2.sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 8,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(1.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
