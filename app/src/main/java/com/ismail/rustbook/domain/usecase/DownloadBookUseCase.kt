package com.ismail.rustbook.domain.usecase

import com.ismail.rustbook.domain.repository.BookRepository

class DownloadBookUseCase(private val repository: BookRepository) {
    suspend operator fun invoke(
        language: String,
        url: String,
        onProgress: (Float) -> Unit
    ) = repository.downloadAndExtract(language, url, onProgress)
}
