package com.ismail.rustbook.domain.usecase

import com.ismail.rustbook.domain.model.LanguageInfo
import com.ismail.rustbook.domain.repository.LanguageRepository

class GetLanguagesUseCase(private val repository: LanguageRepository) {
    operator fun invoke(): List<LanguageInfo> = repository.getLanguages()
}
