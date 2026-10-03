package com.coderj45.jaysangmusicplayer.model

import kotlinx.serialization.Serializable

@Serializable
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumArtUrl: String? = null,
    val mediaUri: String? = null,
    val duration: Long = 0L,
    val isLiked: Boolean = false,
    val lyrics: String? = null,
    val note: String? = null
)

@Serializable
data class LyricLine(
    val timeMs: Long,
    val text: String
)

fun parseLrc(lrc: String?): List<LyricLine> {
    if (lrc == null) return emptyList()
    val lines = lrc.lines()
    val lyricLines = mutableListOf<LyricLine>()
    val regex = Regex("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")

    for (line in lines) {
        val match = regex.find(line)
        if (match != null) {
            val min = match.groupValues[1].toLong()
            val sec = match.groupValues[2].toLong()
            val ms = match.groupValues[3].toLong()
            val text = match.groupValues[4].trim()
            
            val timeMs = (min * 60 * 1000) + (sec * 1000) + (if (ms < 100) ms * 10 else ms)
            lyricLines.add(LyricLine(timeMs, text))
        } else if (line.isNotBlank() && !line.startsWith("[")) {
            // Fallback for non-timed lines (just text)
            lyricLines.add(LyricLine(-1L, line.trim()))
        }
    }
    return lyricLines.sortedBy { it.timeMs }
}
