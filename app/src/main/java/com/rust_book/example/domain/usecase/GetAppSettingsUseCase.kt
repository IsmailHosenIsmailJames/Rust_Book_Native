package com.rust_book.example.domain.usecase

import com.rust_book.example.domain.model.AppSettings
import com.rust_book.example.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetAppSettingsUseCase(private val repository: BookRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.getAppSettings()
}
