package com.ismail.rustbook.ui.activity

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Serializable
data class DownloadZipNavigation(
  var downloadZipURL: String,
  var language: String,
  var isComplete: Boolean,
)

@Composable
fun DownloadZip(
  navController: NavHostController,
  downloadZipURL: String,
  language: String,
) {
  var downloadProgress by remember { mutableStateOf<Float?>(null) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      downloadAndUnzip(context, downloadZipURL, language) { progress ->
        downloadProgress = progress
      }
      navController.popBackStack()
      navController.navigate(HomeActivityNavigation(rootIndex = "$language/book/index.html"))
    }
  }

  Scaffold { paddingValues ->
    Box(
      Modifier
        .padding(paddingValues)
        .fillMaxSize(), contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        if (downloadProgress != null) {
          CircularProgressIndicator(progress = { downloadProgress!! })
        } else {
          CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Downloading...")
      }
    }
  }
}

suspend fun downloadAndUnzip(
  context: Context,
  zipUrl: String,
  language: String,
  onProgress: (Float) -> Unit
) {
  withContext(Dispatchers.IO) {
    val url = URL(zipUrl)
    val connection = url.openConnection()
    connection.connect()
    val totalLength = connection.contentLength
    val input = url.openStream()
    val outputDir = File(context.filesDir, language)
    if (!outputDir.exists()) {
      outputDir.mkdirs()
    }
    ZipInputStream(input).use { zipInput ->
      var entry = zipInput.nextEntry
      var downloadedLength = 0L
      while (entry != null) {
        val outputFile = File(outputDir, entry.name)
        if (entry.isDirectory) {
          outputFile.mkdirs()
        } else {
          outputFile.parentFile?.mkdirs()
          FileOutputStream(outputFile).use { outputStream ->
            val buffer = ByteArray(1024)
            var len: Int
            while (zipInput.read(buffer).also { len = it } > 0) {
              outputStream.write(buffer, 0, len)
              downloadedLength += len
              if (totalLength > 0) {
                onProgress(downloadedLength.toFloat() / totalLength)
              }
            }
          }
        }
        zipInput.closeEntry()
        entry = zipInput.nextEntry
      }
    }
    onProgress(1f) // Ensure progress is 100% at the end
  }
}