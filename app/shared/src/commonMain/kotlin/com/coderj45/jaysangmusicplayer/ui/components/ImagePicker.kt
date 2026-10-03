package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (String) -> Unit): () -> Unit
