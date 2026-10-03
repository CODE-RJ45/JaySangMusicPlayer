# Implementation Plan - Change Album Art from Gallery

This plan details the steps to allow users to change a song's album art by picking an image from the gallery and saving it locally.

## User Review Required

> [!IMPORTANT]
> To support image loading across all platforms (Android, Desktop, iOS, Web), I will add **Coil 3** as a dependency.
> To support image picking, I will implement a multiplatform `rememberImagePickerLauncher`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/gradle/libs.versions.toml)
- Add Coil 3 versions and libraries.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/build.gradle.kts)
- Add Coil dependencies to `commonMain`.

---

### Core Model & Logic

#### [MODIFY] [MusicViewModel.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/commonMain/kotlin/com/coderj45/jaysangmusicplayer/ui/MusicViewModel.kt)
- Add `updateAlbumArt(songId: String, localPath: String)` method.
- This will update the `songs` list and the `currentSong` state.

---

### Platform Interop (Image Picker)

#### [NEW] [ImagePicker.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/commonMain/kotlin/com/coderj45/jaysangmusicplayer/ui/components/ImagePicker.kt)
- Define `expect fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit`.

#### [NEW] [ImagePicker.android.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/androidMain/kotlin/com/coderj45/jaysangmusicplayer/ui/components/ImagePicker.android.kt)
- Implement `actual fun rememberImagePickerLauncher` using `rememberLauncherForActivityResult` with `PickVisualMedia`.

#### [NEW] [ImagePicker.jvm.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/jvmMain/kotlin/com/coderj45/jaysangmusicplayer/ui/components/ImagePicker.jvm.kt)
- Implement a placeholder or basic file picker for Desktop.

#### [NEW] [ImagePicker.ios.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/iosMain/kotlin/com/coderj45/jaysangmusicplayer/ui/components/ImagePicker.ios.kt)
- Implement placeholder for iOS.

---

### UI Components

#### [MODIFY] [PlayerScreen.kt](file:///C:/Users/llanerarjay45/AndroidStudioProjects/JaySangMusicPlayer/app/shared/src/commonMain/kotlin/com/coderj45/jaysangmusicplayer/ui/screens/PlayerScreen.kt)
- Use `AsyncImage` from Coil to display the album art.
- Use `rememberImagePickerLauncher` to trigger the gallery when the album art box is clicked.

## Verification Plan

### Automated Tests
- N/A (UI-driven feature).

### Manual Verification
1. Run the Android app.
2. Open the Player screen.
3. Tap on the "Album Art" placeholder.
4. Select an image from the gallery.
5. Verify the album art updates with the selected image.
