package com.coderj45.jaysangmusicplayer.ui.components

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun CommonWebView(
    url: String, 
    modifier: Modifier,
    onCanGoBackChanged: (Boolean) -> Unit,
    backTrigger: Int
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Handle back trigger from parent
    LaunchedEffect(backTrigger) {
        if (backTrigger > 0 && webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        }
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    javaScriptCanOpenWindowsAutomatically = true
                    allowFileAccess = true
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onCanGoBackChanged(view?.canGoBack() ?: false)
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        return false
                    }
                }
                
                setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
                    try {
                        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                            setMimeType(mimetype)
                            val cookies = CookieManager.getInstance().getCookie(downloadUrl)
                            addRequestHeader("cookie", cookies)
                            addRequestHeader("User-Agent", userAgent)
                            
                            val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                            setTitle(fileName)
                            setDescription("Downloading music...")
                            
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        }
                        
                        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                            context.startActivity(intent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
                
                loadUrl(url)
            }
        },
        modifier = modifier
    )
}
