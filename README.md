# 🎵 JaySang Music Player

**JaySang Music Player** is a beautiful, aesthetic, and professional music player built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. It is designed to provide a premium, offline-first music experience across Android, iOS, and Desktop.

---

## ❤️ Dedication
This application is dedicated to my Love, **Rosa Alma S. Buendia**. 
*Thank you for being my inspiration and the melody to my life's song. I love you!*

**Developed by:** Ar-jay F. Llanera

---

## ✨ Key Features

### 🎨 Aesthetic & Modern Design
- **Dynamic Backgrounds**: The UI colors automatically shift to match the dominant color of the current song's album art.
- **Flippable Album Art**: Tap the album art to flip it and reveal your personal notes for that song.
- **Premium Typography**: Clean, bold typography with a focus on branding and readability.
- **Animated Splash Screen**: A high-end entry experience with bouncy logo animations.

### 🎤 Synced Lyrics
- **Karaoke Style**: Lyrics highlight and scroll automatically in sync with the music.
- **LRC Support**: Standard timestamped lyrics are fully supported.
- **Auto-Fetch**: Integrated with **LRCLib** to automatically download synced lyrics for your music with one tap.

### 📦 Full Package Share (Backup & Restore)
- **All-in-One Backup**: Bundle your songs, playlists, personal notes, and lyrics into a single shareable `.jsang` (ZIP) package.
- **Smart Restore**: Move your entire music library to another phone seamlessly.
- **Security First**: Built-in protection against path traversal (Zip Slip) and memory-efficient streaming for large libraries.

### 🔍 Integrated Search & Download
- **Music Browser**: Search and download new music directly within the app using the integrated MP3Juice browser.
- **In-App Downloader**: Uses the Android DownloadManager to save music directly to your device without leaving the app.

### 📚 Smart Library Management
- **Auto-Scan**: Automatically detects newly added music on your phone and refreshes the library instantly.
- **Permanent Removal**: "Remember" deleted songs so they don't reappear during scans (with a restore option in Settings).
- **Grid Thumbnails**: Playlists show a beautiful 2x2 grid of the songs contained within.

---

## 🛠️ Technical Stack
- **Framework**: Kotlin Multiplatform & Compose Multiplatform
- **Android Audio**: Google Media3 (ExoPlayer) with full Notification/Lockscreen sync.
- **iOS Audio**: AVFoundation (AVPlayer) for native performance.
- **Storage**: Multi-platform persistence (SharedPreferences, NSUserDefaults, JVM Preferences).
- **Networking**: Ktor Client for lyrics fetching.
- **Images**: Coil 3 for high-performance image loading and caching.

---

## 🚀 How to Run

### Android
```powershell
./gradlew :app:androidApp:assembleDebug
```

### Desktop (Windows/Mac/Linux)
```powershell
./gradlew :app:desktopApp:run
```

### iOS
1. Open the `iosApp/iosApp.xcworkspace` in **Xcode**.
2. Select your physical iPhone or a Simulator.
3. Click **Run**.

---

## ⚙️ Settings & Customization
- **Gapless Playback**: Enable/Disable silence between tracks.
- **Restore Library**: Easily undo permanent song removals.
- **Personalized Branding**: Aesthetic "JAYSANG MUSIC" logo integration.

---
*© 2026 Ar-jay F. Llanera. All Rights Reserved.*
