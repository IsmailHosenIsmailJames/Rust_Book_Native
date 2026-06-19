package com.ismail.rustbook.domain.repository

import com.ismail.rustbook.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun downloadAndExtract(
        language: String,
        url: String,
        onProgress: (Float) -> Unit
    )
    
    fun getAppSettings(): Flow<AppSettings>
    suspend fun updateLastOpenedPage(page: String)
    suspend fun updateHomePage(page: String)
    suspend fun toggleFavorite(page: String)
    suspend fun addToHistory(page: String)
    suspend fun isFavorite(page: String): Boolean
    suspend fun resetSettings()
    suspend fun getHtmlFiles(baseDir: String): List<String>
}
