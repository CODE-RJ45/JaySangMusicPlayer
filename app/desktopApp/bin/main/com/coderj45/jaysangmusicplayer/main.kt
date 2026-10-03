package com.coderj45.jaysangmusicplayer

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.coderj45.jaysangmusicplayer.player.JVMAudioPlayer
import com.coderj45.jaysangmusicplayer.data.JVMStorage
import com.coderj45.jaysangmusicplayer.ui.JVMShareManager

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "JaySangMusicPlayer",
    ) {
        val audioPlayer = remember { JVMAudioPlayer() }
        val storage = remember { JVMStorage() }
        val shareManager = remember { JVMShareManager() }

        App(
            audioPlayer = audioPlayer,
            storage = storage,
            shareManager = shareManager
        )
    }
}
