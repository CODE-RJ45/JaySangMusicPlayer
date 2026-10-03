package com.coderj45.jaysangmusicplayer.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import com.coderj45.jaysangmusicplayer.model.BackupData
import com.coderj45.jaysangmusicplayer.model.Song
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import android.content.Intent
import android.os.Environment

class AndroidShareManager(private val context: Context) : ShareManager {

    override suspend fun saveStoryImageToGallery(song: Song): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = createStoryImage(song)
                saveBitmapToGallery(bitmap, "JaySang_Share_${System.currentTimeMillis()}")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override suspend fun exportFullPackage(backupData: BackupData): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "backups")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                
                val zipFile = File(cacheDir, "JaySang_Backup_${System.currentTimeMillis()}.jsang")
                ZipOutputStream(FileOutputStream(zipFile)).use { out ->
                    // 1. Add Metadata
                    val metadataEntry = ZipEntry("metadata.json")
                    out.putNextEntry(metadataEntry)
                    val metadataJson = json.encodeToString(BackupData.serializer(), backupData)
                    out.write(metadataJson.toByteArray())
                    out.closeEntry()

                    // 2. Add Music Files
                    backupData.songs.forEach { song ->
                        song.mediaUri?.let { uriStr ->
                            try {
                                val uri = Uri.parse(uriStr)
                                val inputStream = context.contentResolver.openInputStream(uri)
                                if (inputStream != null) {
                                    val safeTitle = song.title.replace(Regex("[^a-zA-Z0-9]"), "_")
                                    val fileName = "${song.id}___$safeTitle.mp3"
                                    out.putNextEntry(ZipEntry("music/$fileName"))
                                    inputStream.use { it.copyTo(out) }
                                    out.closeEntry()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                shareFile(zipFile)
                zipFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun importFullPackage(uriStr: String): BackupData? {
        return withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriStr)
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                
                val importDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
                val musicDir = File(importDir, "ImportedSongs")
                if (!musicDir.exists()) musicDir.mkdirs()

                var backupData: BackupData? = null
                val songPathMap = mutableMapOf<String, String>()

                ZipInputStream(inputStream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        // Security Check: Zip Slip Vulnerability
                        val canonicalPath = File(musicDir, entry.name).canonicalPath
                        if (!canonicalPath.startsWith(musicDir.canonicalPath)) {
                            throw SecurityException("Illegal zip entry: ${entry.name}")
                        }

                        when {
                            entry.name == "metadata.json" -> {
                                val content = zip.bufferedReader().readText()
                                backupData = json.decodeFromString(BackupData.serializer(), content)
                            }
                            entry.name.startsWith("music/") && !entry.isDirectory -> {
                                val fileName = entry.name.substringAfter("music/")
                                val destFile = File(musicDir, fileName)
                                
                                // Efficiently stream the file instead of reading all bytes into memory
                                FileOutputStream(destFile).use { out ->
                                    val buffer = ByteArray(4096)
                                    var len: Int
                                    while (zip.read(buffer).also { len = it } > 0) {
                                        out.write(buffer, 0, len)
                                    }
                                }
                                // Map the original song ID to new path
                                val songId = fileName.substringBefore("___")
                                songPathMap[songId] = Uri.fromFile(destFile).toString()
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }

                // Update song URIs to the new local paths
                backupData?.let { data ->
                    val updatedSongs = data.songs.map { song ->
                        songPathMap[song.id]?.let { newUri ->
                            song.copy(mediaUri = newUri)
                        } ?: song
                    }
                    data.copy(songs = updatedSongs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share JaySang Backup")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap, filename: String): String? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/JaySangMusic")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return try {
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                it.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun createStoryImage(song: Song): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw "Screenshot" of Player UI
        drawPlayerBackground(canvas, height)
        drawPlayerUI(canvas, song, width)

        // 2. Draw Aesthetic Sticky Note
        drawStickyNote(canvas, song.note ?: "Currently playing...", width, height)

        return bitmap
    }

    private fun drawPlayerBackground(canvas: Canvas, height: Int) {
        val paint = Paint()
        paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), 
            intArrayOf(Color.parseColor("#535353"), Color.BLACK),
            null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, 1080f, height.toFloat(), paint)
    }

    private suspend fun drawPlayerUI(canvas: Canvas, song: Song, width: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val horizontalMargin = 100f
        
        // Draw Header
        paint.color = Color.WHITE
        paint.alpha = 150
        paint.textSize = 35f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NOW PLAYING", width / 2f, 180f, paint)

        // Draw Album Art
        val albumArt = loadAlbumArt(song)
        val artSize = 880
        val artLeft = (width - artSize) / 2f
        val artTop = 320f
        val artRect = RectF(artLeft, artTop, artLeft + artSize, artTop + artSize)
        
        if (albumArt != null) {
            // Apply slight rounding to art
            val path = Path().apply {
                addRoundRect(artRect, 30f, 30f, Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(albumArt, null, artRect, paint)
            canvas.restore()
        } else {
            paint.color = Color.parseColor("#3E3E3E")
            canvas.drawRoundRect(artRect, 30f, 30f, paint)
            paint.color = Color.WHITE
            paint.textSize = 50f
            canvas.drawText("JaySang", width / 2f, artTop + artSize / 2f, paint)
        }

        // Song Title
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.WHITE
        paint.alpha = 255
        paint.textSize = 72f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        val titleTop = artTop + artSize + 140f
        val displayTitle = if (song.title.length > 20) song.title.take(18) + "..." else song.title
        canvas.drawText(displayTitle, horizontalMargin, titleTop, paint)

        // Artist
        paint.textSize = 48f
        paint.color = Color.parseColor("#B3B3B3")
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(song.artist, horizontalMargin, titleTop + 80f, paint)

        // Seek Bar (Mock)
        val seekTop = titleTop + 180f
        val seekWidth = 880f
        paint.color = Color.WHITE
        paint.alpha = 50
        canvas.drawRoundRect(horizontalMargin, seekTop, horizontalMargin + seekWidth, seekTop + 8f, 4f, 4f, paint)
        paint.alpha = 255
        canvas.drawRoundRect(horizontalMargin, seekTop, horizontalMargin + seekWidth * 0.4f, seekTop + 8f, 4f, 4f, paint)
        canvas.drawCircle(horizontalMargin + seekWidth * 0.4f, seekTop + 4f, 12f, paint)

        // Player Controls
        val controlsTop = seekTop + 180f
        val centerY = controlsTop
        val centerX = width / 2f
        val spacing = 180f

        // 1. Shuffle (Leftmost)
        paint.color = Color.WHITE
        paint.alpha = 180
        val shuffleX = centerX - 2 * spacing
        drawShuffleIcon(canvas, shuffleX, centerY, 30f, paint)

        // 2. Previous
        paint.alpha = 255
        val prevX = centerX - spacing
        drawPrevIcon(canvas, prevX, centerY, 35f, paint)

        // 3. Play Button (Center)
        paint.alpha = 255
        canvas.drawCircle(centerX, centerY, 75f, paint)
        paint.color = Color.BLACK
        val playPath = Path().apply {
            moveTo(centerX - 20f, centerY - 30f)
            lineTo(centerX + 35f, centerY)
            lineTo(centerX - 20f, centerY + 30f)
            close()
        }
        canvas.drawPath(playPath, paint)

        // 4. Next
        paint.color = Color.WHITE
        val nextX = centerX + spacing
        drawNextIcon(canvas, nextX, centerY, 35f, paint)

        // 5. Repeat (Rightmost)
        paint.alpha = 180
        val repeatX = centerX + 2 * spacing
        drawRepeatIcon(canvas, repeatX, centerY, 30f, paint)
    }

    private fun drawShuffleIcon(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        canvas.save()
        canvas.translate(x, y)
        val path = Path().apply {
            moveTo(-size, -size / 2)
            lineTo(-size / 2, -size / 2)
            lineTo(size / 2, size / 2)
            lineTo(size, size / 2)
            
            moveTo(-size, size / 2)
            lineTo(-size / 2, size / 2)
            lineTo(size / 2, -size / 2)
            lineTo(size, -size / 2)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.FILL
        canvas.restore()
    }

    private fun drawPrevIcon(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(x + size / 2, y - size)
            lineTo(x - size / 2, y)
            lineTo(x + size / 2, y + size)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawRect(x - size - 5f, y - size, x - size + 5f, y + size, paint)
    }

    private fun drawNextIcon(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(x - size / 2, y - size)
            lineTo(x + size / 2, y)
            lineTo(x - size / 2, y + size)
            close()
        }
        canvas.drawPath(path, paint)
        canvas.drawRect(x + size - 5f, y - size, x + size + 5f, y + size, paint)
    }

    private fun drawRepeatIcon(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        val rect = RectF(x - size, y - size / 1.5f, x + size, y + size / 1.5f)
        canvas.drawRoundRect(rect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL
        // Add a small arrow head to imply repeat
        val arrow = Path().apply {
            moveTo(x + size, y)
            lineTo(x + size + 10f, y - 10f)
            lineTo(x + size - 10f, y - 10f)
            close()
        }
        canvas.drawPath(arrow, paint)
    }

    private fun drawStickyNote(canvas: Canvas, note: String, width: Int, height: Int) {
        val noteWidth = 380f
        val noteHeight = 320f
        
        canvas.save()
        // Positioned higher and more to the right to avoid covering the art center
        canvas.translate(width - noteWidth - 50f, 220f)
        canvas.rotate(6f) 

        val noteRect = RectF(0f, 0f, noteWidth, noteHeight)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Shadow for realism
        paint.setShadowLayer(15f, 8f, 8f, Color.parseColor("#30000000"))
        
        // Paper texture color (Post-it yellow)
        paint.color = Color.parseColor("#FFF9C4") 
        canvas.drawRect(noteRect, paint)
        paint.clearShadowLayer()
        
        // "Tape" effect at the top
        val tapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tapePaint.color = Color.WHITE
        tapePaint.alpha = 180
        canvas.drawRect(noteWidth / 2f - 60f, -15f, noteWidth / 2f + 60f, 25f, tapePaint)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.parseColor("#212121")
        textPaint.textSize = 34f // Smaller text for smaller note
        textPaint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)

        val margin = 40f
        val staticLayout = StaticLayout.Builder.obtain(note, 0, note.length, textPaint, (noteWidth - 2 * margin).toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(6f, 1f)
            .setMaxLines(8)
            .build()

        canvas.translate(margin, margin)
        staticLayout.draw(canvas)
        
        canvas.restore()
    }

    private suspend fun loadAlbumArt(song: Song): Bitmap? {
        if (song.albumArtUrl == null) return null
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(song.albumArtUrl)
                .allowHardware(false) 
                .build()
            val result = loader.execute(request)
            result.image?.toBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
