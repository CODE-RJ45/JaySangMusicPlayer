package com.coderj45.jaysangmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.DownloadManager
import com.coderj45.jaysangmusicplayer.data.AndroidStorage
import com.coderj45.jaysangmusicplayer.data.LocalMusicProvider
import com.coderj45.jaysangmusicplayer.model.Song
import com.coderj45.jaysangmusicplayer.player.AndroidAudioPlayer
import com.coderj45.jaysangmusicplayer.ui.AndroidShareManager

class MainActivity : ComponentActivity() {

    private var localSongsState = mutableStateListOf<Song>()
    private var openPlayerRequest = mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("OPEN_PLAYER", false)) {
            openPlayerRequest.value = true
        }
    }

    private val mediaObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadLocalSongs()
        }
    }

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id != -1L) {
                    // Trigger a refresh
                    loadLocalSongs()
                    // Delay slightly for MediaStore to catch up
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadLocalSongs()
                    }, 3000)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            loadLocalSongs()
            registerMediaObserver()
        }
    }

    private var onFilePicked: ((String) -> Unit)? = null

    private val pickBackupLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFilePicked?.invoke(it.toString()) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        if (intent?.getBooleanExtra("OPEN_PLAYER", false) == true) {
            openPlayerRequest.value = true
        }

        setContent {
            val audioPlayer = remember { AndroidAudioPlayer(applicationContext) }
            val storage = remember { AndroidStorage(applicationContext) }
            val shareManager = remember { AndroidShareManager(applicationContext) }
            val localSongs = remember { localSongsState }
            
            var pendingImportUri by remember { mutableStateOf<String?>(null) }
            onFilePicked = { uri -> pendingImportUri = uri }

            DisposableEffect(Unit) {
                checkAndRequestPermissions()
                
                val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(downloadReceiver, filter, RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(downloadReceiver, filter)
                }

                onDispose {
                    unregisterReceiver(downloadReceiver)
                }
            }
            
            App(
                audioPlayer = audioPlayer, 
                storage = storage, 
                shareManager = shareManager, 
                initialSongs = localSongs,
                onImportBackup = { pickBackupLauncher.launch("*/*") },
                importedBackupUri = pendingImportUri,
                onImportConsumed = { pendingImportUri = null },
                onRefreshSongs = { loadLocalSongs() },
                openPlayerInitially = openPlayerRequest.value,
                onOpenPlayerConsumed = { openPlayerRequest.value = false }
            )

            // We need a way to pass pendingImportUri to the ViewModel inside App
            // I'll add a parameter to App for this or App can handle it internally if I update App.kt again.
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh on resume in case MediaStore was updated while app was in background
        if (hasPermission()) {
            loadLocalSongs()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(mediaObserver)
    }

    private fun hasPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                loadLocalSongs()
                registerMediaObserver()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun registerMediaObserver() {
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaObserver
        )
    }

    private fun loadLocalSongs() {
        val provider = LocalMusicProvider(applicationContext)
        val songs = provider.fetchLocalSongs()
        
        // Only update if there's a change to prevent unnecessary recompositions
        if (localSongsState.size != songs.size || localSongsState.map { it.id } != songs.map { it.id }) {
            localSongsState.clear()
            localSongsState.addAll(songs)
        }
    }
}
