package com.coderj45.jaysangmusicplayer.ui

import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.BackupData

class JVMShareManager : ShareManager {
    override suspend fun saveStoryImageToGallery(song: Song): String? {
        // Desktop implementation for saving image
        return null
    }

    override suspend fun exportFullPackage(backupData: BackupData): String? {
        // Desktop implementation for backup export
        return null
    }

    override suspend fun importFullPackage(uri: String): BackupData? {
        // Desktop implementation for backup import
        return null
    }
}
