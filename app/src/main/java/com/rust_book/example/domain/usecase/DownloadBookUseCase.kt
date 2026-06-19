package com.rust_book.example.domain.usecase

import com.rust_book.example.domain.repository.BookRepository

class DownloadBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(
        language: String,
        url: String,
        onProgress: (Float) -> Unit
    ) = repository.downloadAndExtract(language, url, onProgress)
}
