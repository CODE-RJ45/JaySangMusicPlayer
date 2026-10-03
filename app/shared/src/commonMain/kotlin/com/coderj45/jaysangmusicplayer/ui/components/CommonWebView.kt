package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CommonWebView(
    url: String, 
    modifier: Modifier = Modifier,
    onCanGoBackChanged: (Boolean) -> Unit = {},
    backTrigger: Int = 0
)
