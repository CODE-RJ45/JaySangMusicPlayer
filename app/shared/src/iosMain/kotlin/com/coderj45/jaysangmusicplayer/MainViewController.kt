package com.coderj45.jaysangmusicplayer

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.remember
import com.coderj45.jaysangmusicplayer.player.IOSAudioPlayer
import com.coderj45.jaysangmusicplayer.data.IOSStorage
import com.coderj45.jaysangmusicplayer.ui.IOSShareManager

fun MainViewController() = ComposeUIViewController { 
    val audioPlayer = remember { IOSAudioPlayer() }
    val storage = remember { IOSStorage() }
    val shareManager = remember { IOSShareManager() }
    
    App(
        audioPlayer = audioPlayer,
        storage = storage,
        shareManager = shareManager
    )
}
