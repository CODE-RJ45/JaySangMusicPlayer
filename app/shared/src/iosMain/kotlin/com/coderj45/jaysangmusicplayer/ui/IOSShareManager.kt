package com.coderj45.jaysangmusicplayer.ui

import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.model.BackupData

class IOSShareManager : ShareManager {
    override suspend fun saveStoryImageToGallery(song: Song): String? {
        // iOS implementation for saving/sharing image
        return null
    }

    override suspend fun exportFullPackage(backupData: BackupData): String? {
        // iOS implementation for backup export
        return null
    }

    override suspend fun importFullPackage(uri: String): BackupData? {
        // iOS implementation for backup import
        return null
    }
}
