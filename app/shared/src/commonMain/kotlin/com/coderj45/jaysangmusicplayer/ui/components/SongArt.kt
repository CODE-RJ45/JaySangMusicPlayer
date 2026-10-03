package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun SongArt(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 12,
    seed: String = ""
) {
    val bgColor = remember(seed) {
        if (seed.isEmpty()) Color(0xFF3E3E3E)
        else {
            val hash = seed.hashCode()
            val r = (hash and 0xFF0000 shr 16) % 100 + 40
            val g = (hash and 0x00FF00 shr 8) % 100 + 40
            val b = (hash and 0x0000FF) % 100 + 40
            Color(r, g, b)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val defaultArt = getDefaultArt()
            if (defaultArt != null) {
                AsyncImage(
                    model = defaultArt,
                    contentDescription = "Default Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // High-end Branded Default Photo Fallback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(bgColor, Color.Black)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "JAYSANG",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.2f),
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}
