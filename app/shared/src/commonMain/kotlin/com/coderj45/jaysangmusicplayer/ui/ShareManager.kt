package com.coderj45.jaysangmusicplayer.ui

import com.coderj45.jaysangmusicplayer.model.BackupData
import com.coderj45.jaysangmusicplayer.model.Song

interface ShareManager {
    suspend fun saveStoryImageToGallery(song: Song): String?
    suspend fun exportFullPackage(backupData: BackupData): String?
    suspend fun importFullPackage(uri: String): BackupData?
}
