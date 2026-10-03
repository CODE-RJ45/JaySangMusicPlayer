package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getDefaultArt(): Any? {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier("logo", "drawable", context.packageName)
    return if (resourceId != 0) resourceId else null
}
