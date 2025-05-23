package com.ismail.rustbook.ui.activity

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
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
  var isDownloading by remember { mutableStateOf(true) }
  var showError by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var retryTrigger by remember { mutableStateOf(0) } // Key to re-trigger LaunchedEffect

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  var downloadJob by remember { mutableStateOf<Job?>(null) }

  LaunchedEffect(retryTrigger) { // Re-run when retryTrigger changes
    isDownloading = true
    showError = false
    errorMessage = null
    downloadProgress = null // Reset progress for retry

    downloadJob = coroutineScope.launch {
      val success = downloadAndUnzip(context, downloadZipURL, language) { progress ->
        downloadProgress = progress
      }
      if (success) {
        isDownloading = false
        navController.popBackStack()
        navController.navigate(HomeActivityNavigation(rootIndex = "$language/book/index.html"))
      } else {
        isDownloading = false
        showError = true
        errorMessage = "Download failed. Please check your connection and try again."
        Log.e("DownloadZip", "Download and extraction failed for $language from $downloadZipURL")
      }
    }
  }

  // Handle back press during download: attempt to cancel
  // This is a simplified example; a more robust solution might involve a BackHandler
  SideEffect {
      // This is not a standard way to handle back press for cancellation
      // but demonstrates an idea. A BackHandler Composable is preferred.
  }


  Scaffold { paddingValues ->
    Box(
      Modifier
        .padding(paddingValues)
        .fillMaxSize(), contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
      ) {
        if (isDownloading) {
          if (downloadProgress != null) {
            CircularProgressIndicator(progress = { downloadProgress!! })
          } else {
            CircularProgressIndicator()
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text("Downloading...")
          Spacer(modifier = Modifier.height(16.dp))
          Button(onClick = {
            downloadJob?.cancel()
            isDownloading = false
            // Optionally show a "Cancelled" message or just pop back
            Log.d("DownloadZip", "Download cancelled by user.")
            navController.popBackStack()
          }) {
            Text("Cancel")
          }
        } else if (showError) {
          Text(
            text = errorMessage ?: "An unknown error occurred.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          Button(onClick = {
            retryTrigger++ // Re-trigger the download
          }) {
            Text("Try Again")
          }
          Spacer(modifier = Modifier.height(8.dp))
          Button(onClick = {
            navController.popBackStack() // Go back if error is shown
          }) {
            Text("Close")
          }
        }
      }
    }
  }
}


suspend fun downloadAndUnzip(
  context: Context,
  zipUrl: String,
  language: String,
  onProgress: (Float) -> Unit
): Boolean {
  return withContext(Dispatchers.IO) {
    // Moved import statements here to avoid being at the top-level of the file
    // which can sometimes cause issues if they are not used elsewhere in the file.
    // For clarity, it's generally better to have them at the top of the file.
    // However, if the linter or compiler is picky, this can be a workaround.
    Log.d("DownloadZip", "Starting download and extraction for $language from $zipUrl")
    try {
      val url = URL(zipUrl)
      val connection = url.openConnection()
      connection.connectTimeout = 15000 // 15 seconds timeout
      connection.readTimeout = 15000 // 15 seconds timeout
      connection.connect()

      if (!isActive) {
        Log.d("DownloadZip", "Download cancelled before starting.")
        return@withContext false
      }

      val totalLength = connection.contentLength
      val input = url.openStream()
      val outputDir = File(context.filesDir, language)
      if (!outputDir.exists()) {
        outputDir.mkdirs()
      }

      ZipInputStream(input).use { zipInput ->
        var entry = zipInput.nextEntry
        var downloadedLength = 0L
        while (entry != null && isActive) {
          Log.d("DownloadZip", "Extracting entry: ${entry.name}")
          val outputFile = File(outputDir, entry.name)
          if (entry.isDirectory) {
            outputFile.mkdirs()
          } else {
            outputFile.parentFile?.mkdirs()
            try {
              java.io.FileOutputStream(outputFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var len: Int=0
                while (isActive && zipInput.read(buffer).also { len = it } > 0) {
                  outputStream.write(buffer, 0, len)
                  downloadedLength += len
                  if (totalLength > 0) {
                    onProgress(downloadedLength.toFloat() / totalLength * 0.9f) // Downloading is 90% of the work
                  }
                }
                if (!isActive) {
                  Log.d("DownloadZip", "Download cancelled during file writing.")
                  // It's good practice to delete partially written file
                  outputFile.delete()
                  return@withContext false
                }
              }
            } catch (e: java.io.IOException) {
              Log.e("DownloadZip", "Error writing file ${outputFile.path}", e)
              outputFile.delete() // Clean up partially written file
              return@withContext false
            }
          }
          zipInput.closeEntry()
          if (isActive) { // Check before getting next entry
            entry = zipInput.nextEntry
          } else {
            Log.d("DownloadZip", "Download cancelled during zip entry iteration.")
            return@withContext false
          }
        }
        if (!isActive) {
          Log.d("DownloadZip", "Download cancelled after processing entries.")
          return@withContext false
        }
      }
      // Simulate extraction progress if needed, or just mark as complete for this part
      // For simplicity, we'll assume extraction is the remaining 10%
      onProgress(0.95f) // Nearing completion

      Log.d("DownloadZip", "Download and extraction completed successfully for $language.")
      onProgress(1f) // Ensure progress is 100% at the end
      true
    } catch (e: java.net.UnknownHostException) {
      Log.e("DownloadZip", "Network error: Unknown host", e)
      false
    } catch (e: java.net.SocketTimeoutException) {
      Log.e("DownloadZip", "Network error: Socket timeout", e)
      false
    } catch (e: java.util.zip.ZipException) {
      Log.e("DownloadZip", "ZIP error during extraction", e)
      false
    } catch (e: java.io.IOException) {
      Log.e("DownloadZip", "IO error during download/extraction", e)
      false
    } catch (e: Exception) {
      Log.e("DownloadZip", "An unexpected error occurred", e)
      false
    }
  }
}