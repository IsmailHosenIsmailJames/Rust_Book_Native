package com.rust_book.example.domain.usecase

import com.rust_book.example.domain.repository.BookRepository

class UpdateAppSettingsUseCase(private val repository: BookRepository) {
    suspend fun updateLastOpenedPage(page: String) = repository.updateLastOpenedPage(page)
    suspend fun updateHomePage(page: String) = repository.updateHomePage(page)
    suspend fun toggleFavorite(page: String) = repository.toggleFavorite(page)
    suspend fun addToHistory(page: String) = repository.addToHistory(page)
    suspend fun resetSettings() = repository.resetSettings()
}
