package com.coderj45.jaysangmusicplayer.ui

import com.coderj45.jaysangmusicplayer.model.mockSongs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MusicViewModelTest {

    @Test
    fun testInitialState() {
        val viewModel = MusicViewModel()
        assertEquals(mockSongs, viewModel.songs.value)
        assertEquals(null, viewModel.currentSong.value)
        assertFalse(viewModel.isPlaying.value)
        assertEquals(0L, viewModel.playbackPosition.value)
    }

    @Test
    fun testSelectSong() {
        val viewModel = MusicViewModel()
        val song = mockSongs[0]
        viewModel.selectSong(song)
        assertEquals(song, viewModel.currentSong.value)
        assertTrue(viewModel.isPlaying.value)
        assertEquals(0L, viewModel.playbackPosition.value)
    }

    @Test
    fun testTogglePlayPause() {
        val viewModel = MusicViewModel()
        val song = mockSongs[0]
        viewModel.selectSong(song)
        assertTrue(viewModel.isPlaying.value)
        
        viewModel.togglePlayPause()
        assertFalse(viewModel.isPlaying.value)
        
        viewModel.togglePlayPause()
        assertTrue(viewModel.isPlaying.value)
    }

    @Test
    fun testPlayNext() {
        val viewModel = MusicViewModel()
        viewModel.selectSong(mockSongs[0])
        viewModel.playNext()
        assertEquals(mockSongs[1], viewModel.currentSong.value)
    }

    @Test
    fun testPlayPrevious() {
        val viewModel = MusicViewModel()
        viewModel.selectSong(mockSongs[1])
        viewModel.playPrevious()
        assertEquals(mockSongs[0], viewModel.currentSong.value)
    }
}
