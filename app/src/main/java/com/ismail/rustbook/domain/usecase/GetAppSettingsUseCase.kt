package com.ismail.rustbook.domain.usecase

import com.ismail.rustbook.domain.model.AppSettings
import com.ismail.rustbook.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow

class GetAppSettingsUseCase(private val repository: BookRepository) {
    operator fun invoke(): Flow<AppSettings> = repository.getAppSettings()
}
