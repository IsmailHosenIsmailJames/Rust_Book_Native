package com.rust_book.example.data.repository

import android.content.Context
import com.rust_book.example.data.local.AppPreferences
import com.rust_book.example.domain.model.AppSettings
import com.rust_book.example.domain.repository.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream

class BookRepositoryImpl(
    private val context: Context,
    private val appPreferences: AppPreferences
) : BookRepository {

    override suspend fun downloadAndExtract(
        language: String,
        url: String,
        onProgress: (Float) -> Unit
    ) {
        val cacheFile = File(context.cacheDir, "temp_book.zip")
        downloadFile(url, cacheFile) { progress ->
            onProgress(progress * 0.5f) // First 50% for download
        }
        extractZip(cacheFile, language) { progress ->
            onProgress(0.5f + progress * 0.5f) // Next 50% for extraction
        }
    }

    override fun getAppSettings(): Flow<AppSettings> = appPreferences.getAppSettings()

    override suspend fun updateLastOpenedPage(page: String) {
        appPreferences.lastOpenedPage = page
    }

    override suspend fun updateHomePage(page: String) {
        appPreferences.homePage = page
    }

    override suspend fun toggleFavorite(page: String) {
        appPreferences.toggleFavorite(page)
    }

    override suspend fun toggleCompletion(page: String) {
        appPreferences.toggleCompletion(page)
    }

    override suspend fun addToHistory(page: String) {
        appPreferences.addToHistory(page)
    }

    override suspend fun isFavorite(page: String): Boolean {
        return appPreferences.isFavorite(page)
    }

    override suspend fun isCompleted(page: String): Boolean {
        return appPreferences.isCompleted(page)
    }

    override suspend fun resetSettings() {
        appPreferences.resetAll()
    }

    override suspend fun getHtmlFiles(baseDir: String): List<String> = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, baseDir)
        if (!dir.exists()) return@withContext emptyList()
        dir.walkTopDown()
            .filter { it.extension == "html" }
            .map { it.absolutePath.substringAfter(context.filesDir.absolutePath + "/") }
            .toList()
    }

    private suspend fun downloadFile(
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

    private suspend fun extractZip(
        zipFile: File,
        language: String,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val outputDir = File(context.filesDir, language)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val zipInputStream = ZipInputStream(zipFile.inputStream())
        zipInputStream.use { zipInput ->
            var entry = zipInput.nextEntry
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
                onProgress(entriesProcessed.toFloat() / (entriesProcessed + 10))
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
        onProgress(1f)
        zipFile.delete()
    }
}
