package com.ismail.rustbook.ui.activity

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class HomeActivityNavigation(
  var rootIndex: String
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HomeActivity(navController: NavHostController, rootIndex: String) {
  val context = LocalContext.current
  val file = File(context.filesDir, rootIndex)
  Scaffold(
    topBar = {

    }
  ){ paddingValues ->
    Box(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
      AndroidView(
        factory = { context ->
          WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.domStorageEnabled = true

            webViewClient = WebViewClient()
            loadUrl(file.absolutePath)
          }
        }
      )
    }
  }
}