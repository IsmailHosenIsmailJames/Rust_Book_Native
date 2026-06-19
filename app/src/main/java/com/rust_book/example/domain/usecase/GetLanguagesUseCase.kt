package com.rust_book.example.domain.usecase

import com.rust_book.example.domain.model.LanguageInfo
import com.rust_book.example.domain.repository.LanguageRepository

class GetLanguagesUseCase(private val repository: LanguageRepository) {
    operator fun invoke(): List<LanguageInfo> = repository.getLanguages()
}
