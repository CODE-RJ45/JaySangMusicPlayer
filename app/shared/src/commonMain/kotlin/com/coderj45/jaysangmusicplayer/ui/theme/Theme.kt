package com.coderj45.jaysangmusicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SpotifyGreen = Color(0xFF1DB954)
private val SpotifyBlack = Color(0xFF121212)
private val SpotifyDeepBlack = Color(0xFF000000)
private val SpotifyDarkGray = Color(0xFF282828)
private val SpotifyLightGray = Color(0xFFB3B3B3)
private val SpotifyWhite = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    surface = SpotifyBlack,
    onSurface = SpotifyWhite,
    background = SpotifyDeepBlack,
    onBackground = SpotifyWhite,
    secondary = SpotifyLightGray,
    onSecondary = Color.Black,
    surfaceVariant = SpotifyDarkGray,
    onSurfaceVariant = SpotifyLightGray,
    tertiary = SpotifyGreen // Used for some highlights
)

@Composable
fun SpotifyTheme(
    content: @Composable () -> Unit
) {
    val typography = Typography(
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Normal
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            color = SpotifyLightGray
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    )

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = typography,
        content = content
    )
}
