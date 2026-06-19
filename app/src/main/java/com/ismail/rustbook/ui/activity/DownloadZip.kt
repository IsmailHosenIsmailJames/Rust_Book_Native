package com.ismail.rustbook.ui.activity

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

@Serializable
data class DownloadZipNavigation(
  var downloadZipURL: String,
  var language: String,
  var isComplete: Boolean,
)

enum class DownloadStage {
  Downloading,
  Extracting,
  Finished
}

@Composable
fun DownloadZip(
  navController: NavHostController,
  downloadZipURL: String,
  language: String,
) {
  var stage by remember { mutableStateOf(DownloadStage.Downloading) }
  var progress by remember { mutableFloatStateOf(0f) }
  val context = LocalContext.current

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    label = "ProgressAnimation"
  )

  LaunchedEffect(Unit) {
    val cacheFile = File(context.cacheDir, "temp_book.zip")
    
    // Step 1: Download
    downloadFile(downloadZipURL, cacheFile) { currentProgress ->
      progress = currentProgress
    }

    // Step 2: Extract
    stage = DownloadStage.Extracting
    progress = 0f
    extractZip(context, cacheFile, language) { currentProgress ->
      progress = currentProgress
    }

    stage = DownloadStage.Finished
    navController.popBackStack()
    navController.navigate(HomeActivityNavigation(rootIndex = "$language/book/index.html"))
  }

  Scaffold { paddingValues ->
    Box(
      Modifier
        .padding(paddingValues)
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
      ) {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (stage == DownloadStage.Downloading) 
                Icons.Default.Info else Icons.Default.Settings,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
              text = if (stage == DownloadStage.Downloading) 
                "Downloading $language Content" else "Extracting Files...",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
              text = if (stage == DownloadStage.Downloading)
                "Please wait while we fetch the latest version." else "Setting up the offline book content.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
              strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "${(progress * 100).toInt()}%",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}

suspend fun downloadFile(
  zipUrl: String,
  outputFile: File,
  onProgress: (Float) -> Unit
) = withContext(Dispatchers.IO) {
  val url = URL(zipUrl)
  val connection = url.openConnection()
  connection.connect()
  val totalLength = connection.contentLength
  
  url.openStream().use { input ->
    FileOutputStream(outputFile).use { output ->
      val buffer = ByteArray(8192)
      var downloadedLength = 0L
      var len: Int
      while (input.read(buffer).also { len = it } > 0) {
        output.write(buffer, 0, len)
        downloadedLength += len
        if (totalLength > 0) {
          onProgress(downloadedLength.toFloat() / totalLength)
        }
      }
    }
  }
}

suspend fun extractZip(
  context: Context,
  zipFile: File,
  language: String,
  onProgress: (Float) -> Unit
) = withContext(Dispatchers.IO) {
  val outputDir = File(context.filesDir, language)
  if (!outputDir.exists()) {
    outputDir.mkdirs()
  }

  // Count total entries for progress (optional but good for UX)
  // For simplicity, we'll use a rough progress or just finish it.
  // A better way is to know total entries, but that requires reading twice or knowing beforehand.
  // We'll estimate based on file size if possible or just show indeterminate/incremental.
  
  val zipInputStream = ZipInputStream(zipFile.inputStream())
  zipInputStream.use { zipInput ->
    var entry = zipInput.nextEntry
    // Simple incremental progress since we don't know total entries easily without a pre-scan
    var entriesProcessed = 0
    while (entry != null) {
      val outputFile = File(outputDir, entry.name)
      if (entry.isDirectory) {
        outputFile.mkdirs()
      } else {
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { outputStream ->
          val buffer = ByteArray(8192)
          var len: Int
          while (zipInput.read(buffer).also { len = it } > 0) {
            outputStream.write(buffer, 0, len)
          }
        }
      }
      entriesProcessed++
      // Since we don't have total entries, we'll just move it slightly or keep it at 100% at end
      // For now, let's just use 1.0f when done, or fake it.
      onProgress(entriesProcessed.toFloat() / (entriesProcessed + 10)) // Fake incremental
      zipInput.closeEntry()
      entry = zipInput.nextEntry
    }
  }
  onProgress(1f)
  zipFile.delete() // Clean up cache
}
