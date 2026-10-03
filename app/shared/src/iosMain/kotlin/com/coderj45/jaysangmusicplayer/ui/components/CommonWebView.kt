package com.coderj45.jaysangmusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import platform.WebKit.WKWebView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CommonWebView(
    url: String, 
    modifier: Modifier,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int
) {
    UIKitView(
        factory = {
            val webView = WKWebView()
            val request = NSURLRequest(NSURL(string = url))
            webView.loadRequest(request)
            webView
        },
        modifier = modifier
    )
}
