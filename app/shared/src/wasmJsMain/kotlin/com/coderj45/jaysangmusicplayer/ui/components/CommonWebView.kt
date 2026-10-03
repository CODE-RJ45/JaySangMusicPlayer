package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun CommonWebView(
    url: String, 
    modifier: Modifier,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("WebView not supported on WasmJs yet: $url")
    }
}
