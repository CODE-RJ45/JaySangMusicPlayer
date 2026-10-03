package com.coderj45.jaysangmusicplayer.ui.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { 
                val localPath = saveImageLocally(context, it)
                onImagePicked(localPath) 
            }
        }
    )

    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

private fun saveImageLocally(context: Context, uri: android.net.Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "custom_art_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, "album_art").apply { mkdirs() }
        val outputFile = File(file, fileName)
        
        inputStream?.use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output)
            }
        }
        outputFile.absolutePath
    } catch (e: Exception) {
        uri.toString() // Fallback to original URI if copy fails
    }
}
