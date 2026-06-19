package com.ismail.rustbook.domain.usecase

import com.ismail.rustbook.domain.repository.BookRepository

class UpdateAppSettingsUseCase(private val repository: BookRepository) {
    suspend fun updateLastOpenedPage(page: String) = repository.updateLastOpenedPage(page)
    suspend fun updateHomePage(page: String) = repository.updateHomePage(page)
    suspend fun toggleFavorite(page: String) = repository.toggleFavorite(page)
    suspend fun addToHistory(page: String) = repository.addToHistory(page)
    suspend fun resetSettings() = repository.resetSettings()
}
